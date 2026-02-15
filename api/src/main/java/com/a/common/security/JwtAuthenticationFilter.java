package com.a.common.security;

import com.a.entity.UserEntity;
import com.a.repository.RedisOnlineStatusRepository;
import com.a.repository.RedisUserCacheRepository;
import com.a.repository.UserRepository;
import com.a.repository.dto.CachedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RedisUserCacheRepository redisUserCacheRepository;
    private final RedisOnlineStatusRepository redisOnlineStatusRepository;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserId(token);

            // Redis 캐시에서 먼저 확인
            boolean authenticated = false;
            CachedUser cachedUser = redisUserCacheRepository.getUser(userId);
            if (cachedUser != null) {
                if (!cachedUser.deleted()) {
                    UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    authenticated = true;
                }
            } else {
                // 캐시 미스 → DB 조회
                UserEntity user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    redisUserCacheRepository.setUser(CachedUser.from(user));
                    if (user.getDeletedAt() == null) {
                        UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        authenticated = true;
                    }
                }
            }

            // 온라인 상태 기록
            if (authenticated) {
                redisOnlineStatusRepository.markOnline(userId);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("JWT_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}

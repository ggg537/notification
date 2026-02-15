package com.a.common.config;

import com.a.config.redis.RedisKeyConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return true;
        }

        Long userId;
        try {
            userId = (Long) auth.getPrincipal();
        } catch (ClassCastException e) {
            return true;
        }

        String endpoint = request.getMethod() + ":" + handlerMethod.getMethod().getName();
        String key = String.format(RedisKeyConstants.RATE_LIMIT, userId, endpoint);

        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, rateLimit.windowSeconds(), TimeUnit.SECONDS);
            }

            if (count != null && count > rateLimit.maxRequests()) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.setHeader("Retry-After", String.valueOf(rateLimit.windowSeconds()));
                var body = new com.a.common.dto.RateLimitExceededResponse(
                    "Rate limit exceeded", rateLimit.windowSeconds()
                );
                response.getWriter().write(objectMapper.writeValueAsString(body));
                return false;
            }
        } catch (Exception e) {
            // fail-open: Redis 장애 시 요청 허용
            log.warn("Redis rate limit check failed: {}", e.getMessage());
        }

        return true;
    }
}

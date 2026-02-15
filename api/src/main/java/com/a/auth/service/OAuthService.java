package com.a.auth.service;

import com.a.common.security.JwtTokenProvider;
import com.a.entity.RefreshTokenEntity;
import com.a.entity.UserEntity;
import com.a.repository.RedisRefreshTokenRepository;
import com.a.repository.RefreshTokenRepository;
import com.a.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisRefreshTokenRepository redisRefreshTokenRepository;

    @Value("${oauth.google.client-id:}")
    private String googleClientId;

    @Value("${oauth.google.client-secret:}")
    private String googleClientSecret;

    @Value("${oauth.kakao.client-id:}")
    private String kakaoClientId;

    @Value("${oauth.kakao.client-secret:}")
    private String kakaoClientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, String> getClientConfig() {
        return Map.of(
            "googleClientId", googleClientId != null ? googleClientId : "",
            "kakaoClientId", kakaoClientId != null ? kakaoClientId : ""
        );
    }

    @Transactional
    public AuthService.LoginResult processOAuthLogin(String provider, String code, String redirectUri) {
        return switch (provider.toLowerCase()) {
            case "google" -> processGoogleLogin(code, redirectUri);
            case "kakao" -> processKakaoLogin(code, redirectUri);
            default -> throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
        };
    }

    private AuthService.LoginResult processGoogleLogin(String code, String redirectUri) {
        if (!StringUtils.hasText(googleClientId) || !StringUtils.hasText(googleClientSecret)) {
            throw new IllegalArgumentException("Google OAuth is not configured");
        }

        // 인가 코드를 토큰으로 교환
        String tokenUrl = "https://oauth2.googleapis.com/token";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        @SuppressWarnings("unchecked")
        Map<String, Object> tokenResponse = restTemplate.postForObject(
            tokenUrl, new HttpEntity<>(params, headers), Map.class
        );

        String accessToken = (String) tokenResponse.get("access_token");

        // 사용자 정보 조회
        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(accessToken);
        @SuppressWarnings("unchecked")
        Map<String, Object> userInfo = restTemplate.exchange(
            "https://www.googleapis.com/oauth2/v2/userinfo",
            HttpMethod.GET,
            new HttpEntity<>(userInfoHeaders),
            Map.class
        ).getBody();

        String providerId = String.valueOf(userInfo.get("id"));
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String picture = (String) userInfo.get("picture");

        return loginOrCreateUser("google", providerId, email, name, picture);
    }

    private AuthService.LoginResult processKakaoLogin(String code, String redirectUri) {
        if (!StringUtils.hasText(kakaoClientId)) {
            throw new IllegalArgumentException("Kakao OAuth is not configured");
        }

        // 인가 코드를 토큰으로 교환
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", kakaoClientId);
        if (StringUtils.hasText(kakaoClientSecret)) {
            params.add("client_secret", kakaoClientSecret);
        }
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        @SuppressWarnings("unchecked")
        Map<String, Object> tokenResponse = restTemplate.postForObject(
            tokenUrl, new HttpEntity<>(params, headers), Map.class
        );

        String accessToken = (String) tokenResponse.get("access_token");

        // 사용자 정보 조회
        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(accessToken);
        @SuppressWarnings("unchecked")
        Map<String, Object> userInfo = restTemplate.exchange(
            "https://kapi.kakao.com/v2/user/me",
            HttpMethod.GET,
            new HttpEntity<>(userInfoHeaders),
            Map.class
        ).getBody();

        String providerId = String.valueOf(userInfo.get("id"));

        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
        String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;

        @SuppressWarnings("unchecked")
        Map<String, Object> profile = kakaoAccount != null ? (Map<String, Object>) kakaoAccount.get("profile") : null;
        String name = profile != null ? (String) profile.get("nickname") : null;
        String picture = profile != null ? (String) profile.get("profile_image_url") : null;

        if (name == null) name = "Kakao User";

        return loginOrCreateUser("kakao", providerId, email, name, picture);
    }

    private AuthService.LoginResult loginOrCreateUser(
        String provider, String providerId, String email, String name, String picture
    ) {
        // 1. OAuth 제공자 + ID로 검색
        UserEntity user = userRepository.findByOauthProviderAndOauthProviderId(provider, providerId)
            .orElse(null);

        if (user == null && email != null) {
            // 2. 이메일로 검색 후 OAuth 연동
            user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                user.setOauthProvider(provider);
                user.setOauthProviderId(providerId);
                if (!user.isEmailVerified()) {
                    user.setEmailVerified(true);
                }
                userRepository.save(user);
            }
        }

        if (user == null) {
            // 3. 새 사용자 생성
            user = UserEntity.builder()
                .name(name)
                .email(email != null ? email : provider + "_" + providerId + "@oauth.local")
                .oauthProvider(provider)
                .oauthProviderId(providerId)
                .profileImageUrl(picture)
                .emailVerified(true)
                .build();
            user = userRepository.save(user);
        }

        if (user.getDeletedAt() != null) {
            throw new IllegalArgumentException("Account has been deleted");
        }

        // 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = createRefreshToken(user.getId());

        return new AuthService.LoginResult(accessToken, refreshToken, false);
    }

    private String createRefreshToken(Long userId) {
        String token = jwtTokenProvider.generateRefreshToken();
        long expirationMs = jwtTokenProvider.getRefreshExpirationMs();

        // MySQL 기록
        RefreshTokenEntity entity = RefreshTokenEntity.builder()
            .userId(userId)
            .token(token)
            .expiresAt(Instant.now().plusMillis(expirationMs))
            .build();
        refreshTokenRepository.save(entity);

        // Redis 기록
        redisRefreshTokenRepository.saveToken(token, userId);

        return token;
    }
}

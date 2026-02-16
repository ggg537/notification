package com.a.auth.service;

import com.a.common.security.JwtTokenProvider;
import com.a.common.service.EmailService;
import com.a.entity.EmailVerificationTokenEntity;
import com.a.entity.PasswordResetTokenEntity;
import com.a.entity.RefreshTokenEntity;
import com.a.entity.UserEntity;
import com.a.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final RedisRefreshTokenRepository redisRefreshTokenRepository;

    @Transactional
    public UserEntity signup(String email, String password, String name) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        UserEntity user = UserEntity.builder()
            .email(email)
            .password(passwordEncoder.encode(password))
            .name(name)
            .emailVerified(false)
            .build();
        UserEntity saved = userRepository.save(user);

        // 인증 이메일 발송
        String token = UUID.randomUUID().toString();
        emailVerificationTokenRepository.save(
            EmailVerificationTokenEntity.builder()
                .userId(saved.getId())
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build()
        );
        emailService.sendVerificationEmail(email, token);

        return saved;
    }

    @Transactional
    public LoginResult login(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (user.getDeletedAt() != null) {
            throw new IllegalArgumentException("Account has been deleted");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = createRefreshToken(user.getId());

        return new LoginResult(accessToken, refreshToken, !user.isEmailVerified());
    }

    @Transactional
    public LoginResult refresh(String refreshToken) {
        // Redis 우선 조회
        Long redisUserId = redisRefreshTokenRepository.getUserIdByToken(refreshToken);
        if (redisUserId != null) {
            UserEntity user = userRepository.findById(redisUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            if (user.getDeletedAt() != null) {
                redisRefreshTokenRepository.deleteToken(refreshToken);
                refreshTokenRepository.deleteByToken(refreshToken);
                throw new IllegalArgumentException("Account has been deleted");
            }

            redisRefreshTokenRepository.deleteToken(refreshToken);
            refreshTokenRepository.deleteByToken(refreshToken);

            String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
            String newRefreshToken = createRefreshToken(user.getId());
            return new LoginResult(newAccessToken, newRefreshToken, false);
        }

        // MySQL 폴백
        RefreshTokenEntity tokenEntity = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(tokenEntity);
            throw new IllegalArgumentException("Refresh token expired");
        }

        UserEntity user = userRepository.findById(tokenEntity.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getDeletedAt() != null) {
            refreshTokenRepository.delete(tokenEntity);
            throw new IllegalArgumentException("Account has been deleted");
        }

        refreshTokenRepository.delete(tokenEntity);

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = createRefreshToken(user.getId());

        return new LoginResult(newAccessToken, newRefreshToken, false);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null) {
            redisRefreshTokenRepository.deleteToken(refreshToken);
            refreshTokenRepository.deleteByToken(refreshToken);
        }
    }

    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationTokenEntity tokenEntity = emailVerificationTokenRepository.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (tokenEntity.isUsed()) {
            throw new IllegalArgumentException("Token already used");
        }
        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expired");
        }

        UserEntity user = userRepository.findById(tokenEntity.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setEmailVerified(true);
        userRepository.save(user);

        tokenEntity.setUsed(true);
        emailVerificationTokenRepository.save(tokenEntity);
    }

    @Transactional
    public void resendVerification(Long userId) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.isEmailVerified()) {
            throw new IllegalArgumentException("Email already verified");
        }

        emailVerificationTokenRepository.deleteAllByUserId(userId);

        String token = UUID.randomUUID().toString();
        emailVerificationTokenRepository.save(
            EmailVerificationTokenEntity.builder()
                .userId(userId)
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build()
        );
        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getPassword() != null && !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getDeletedAt() != null) return;

            passwordResetTokenRepository.deleteAllByUserId(user.getId());

            String token = UUID.randomUUID().toString();
            passwordResetTokenRepository.save(
                PasswordResetTokenEntity.builder()
                    .userId(user.getId())
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .build()
            );
            emailService.sendPasswordResetEmail(email, token);
        });
        // 이메일 존재 여부를 노출하지 않음
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetTokenEntity tokenEntity = passwordResetTokenRepository.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid reset token"));

        if (tokenEntity.isUsed()) {
            throw new IllegalArgumentException("Token already used");
        }
        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expired");
        }

        UserEntity user = userRepository.findById(tokenEntity.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenEntity.setUsed(true);
        passwordResetTokenRepository.save(tokenEntity);

        // 모든 세션 강제 로그아웃
        redisRefreshTokenRepository.deleteAllByUserId(user.getId());
        refreshTokenRepository.deleteAllByUserId(user.getId());
    }

    private String createRefreshToken(Long userId) {
        String token = jwtTokenProvider.generateRefreshToken();
        long expirationMs = jwtTokenProvider.getRefreshExpirationMs();

        // MySQL 기록
        RefreshTokenEntity entity = RefreshTokenEntity.builder()
            .userId(userId)
            .token(token)
            .expiresAt(LocalDateTime.now().plus(expirationMs, ChronoUnit.MILLIS))
            .build();
        refreshTokenRepository.save(entity);

        // Redis 기록
        redisRefreshTokenRepository.saveToken(token, userId);

        return token;
    }

    public record LoginResult(String accessToken, String refreshToken, boolean emailVerificationRequired) {}
}

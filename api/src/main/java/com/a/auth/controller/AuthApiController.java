package com.a.auth.controller;

import com.a.auth.dto.*;
import com.a.auth.service.AuthService;
import com.a.auth.service.OAuthService;
import com.a.common.dto.MessageResponse;
import com.a.entity.UserEntity;
import com.a.user.service.UserProfileService;
import com.a.user.service.AccountDeletionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthService authService;
    private final UserProfileService userProfileService;
    private final OAuthService oAuthService;
    private final AccountDeletionService accountDeletionService;

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> signup(@RequestBody SignupRequest request) {
        authService.signup(request.email(), request.password(), request.name());
        return ResponseEntity.ok(new MessageResponse("Signup successful. Please check your email for verification."));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
        @RequestBody LoginRequest request,
        HttpServletResponse response
    ) {
        AuthService.LoginResult result = authService.login(request.email(), request.password());

        Cookie accessCookie = new Cookie("JWT_TOKEN", result.accessToken());
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(900);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("REFRESH_TOKEN", result.refreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(604800);
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(new LoginResponse(result.accessToken(), result.emailVerificationRequired()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        String refreshToken = resolveRefreshToken(request);
        if (refreshToken == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            AuthService.LoginResult result = authService.refresh(refreshToken);

            Cookie accessCookie = new Cookie("JWT_TOKEN", result.accessToken());
            accessCookie.setHttpOnly(true);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(900);
            response.addCookie(accessCookie);

            Cookie newRefreshCookie = new Cookie("REFRESH_TOKEN", result.refreshToken());
            newRefreshCookie.setHttpOnly(true);
            newRefreshCookie.setPath("/");
            newRefreshCookie.setMaxAge(604800);
            response.addCookie(newRefreshCookie);

            return ResponseEntity.ok(new LoginResponse(result.accessToken()));
        } catch (IllegalArgumentException e) {
            clearCookies(response);
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        String refreshToken = resolveRefreshToken(request);
        authService.logout(refreshToken);
        clearCookies(response);
        return ResponseEntity.ok(new MessageResponse("Logged out"));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthMeResponse> me(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserEntity user = userProfileService.getUserById(userId);
        return ResponseEntity.ok(AuthMeResponse.from(user));
    }

    // ========== Email Verification ==========

    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        try {
            authService.verifyEmail(token);
            return ResponseEntity.ok(new MessageResponse("Email verified successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        authService.resendVerification(userId);
        return ResponseEntity.ok(new MessageResponse("Verification email sent"));
    }

    // ========== Password Management ==========

    @PutMapping("/password")
    public ResponseEntity<MessageResponse> changePassword(
        @RequestBody ChangePasswordRequest request,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        try {
            authService.changePassword(userId, request.currentPassword(), request.newPassword());
            return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.requestPasswordReset(request.email());
        return ResponseEntity.ok(new MessageResponse("If the email exists, a reset link has been sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request.token(), request.newPassword());
            return ResponseEntity.ok(new MessageResponse("Password reset successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // ========== Account Deletion ==========

    @DeleteMapping("/account")
    public ResponseEntity<MessageResponse> deleteAccount(
        @RequestBody DeleteAccountRequest request,
        Authentication authentication,
        HttpServletResponse response
    ) {
        Long userId = (Long) authentication.getPrincipal();
        try {
            accountDeletionService.deleteAccount(userId, request.password());
            clearCookies(response);
            return ResponseEntity.ok(new MessageResponse("Account deleted"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // ========== OAuth2 ==========

    @PostMapping("/oauth/{provider}")
    public ResponseEntity<LoginResponse> oauthLogin(
        @PathVariable String provider,
        @RequestBody OAuthLoginRequest request,
        HttpServletResponse response
    ) {
        try {
            AuthService.LoginResult result = oAuthService.processOAuthLogin(
                provider, request.code(), request.redirectUri()
            );

            Cookie accessCookie = new Cookie("JWT_TOKEN", result.accessToken());
            accessCookie.setHttpOnly(true);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(900);
            response.addCookie(accessCookie);

            Cookie refreshCookie = new Cookie("REFRESH_TOKEN", result.refreshToken());
            refreshCookie.setHttpOnly(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(604800);
            response.addCookie(refreshCookie);

            return ResponseEntity.ok(new LoginResponse(result.accessToken()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ========== OAuth Config (public) ==========

    @GetMapping("/oauth/config")
    public ResponseEntity<?> getOAuthConfig() {
        return ResponseEntity.ok(oAuthService.getClientConfig());
    }

    // ========== Helpers ==========

    private String resolveRefreshToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("REFRESH_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void clearCookies(HttpServletResponse response) {
        Cookie accessCookie = new Cookie("JWT_TOKEN", "");
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("REFRESH_TOKEN", "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);
    }
}

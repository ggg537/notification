package com.a.auth.dto;

public record LoginResponse(String token, boolean emailVerificationRequired) {
    public LoginResponse(String token) {
        this(token, false);
    }
}

package com.a.auth.dto;

public record ResetPasswordRequest(String token, String newPassword) {}

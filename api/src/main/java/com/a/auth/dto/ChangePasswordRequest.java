package com.a.auth.dto;

public record ChangePasswordRequest(String currentPassword, String newPassword) {}

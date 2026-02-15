package com.a.user.dto;

public record UserUpdateRequest(
    String name,
    String bio,
    String handle
) {
}

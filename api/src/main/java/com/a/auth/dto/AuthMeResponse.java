package com.a.auth.dto;

import com.a.entity.UserEntity;

public record AuthMeResponse(
    Long id,
    String name,
    String email,
    String handle,
    String bio,
    String profileImageUrl
) {
    public static AuthMeResponse from(UserEntity user) {
        return new AuthMeResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getHandle(),
            user.getBio(),
            user.getProfileImageUrl()
        );
    }
}

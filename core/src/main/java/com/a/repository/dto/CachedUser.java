package com.a.repository.dto;

import com.a.entity.UserEntity;

public record CachedUser(
    Long id,
    String name,
    String email,
    String handle,
    String bio,
    String profileImageUrl,
    boolean emailVerified,
    boolean deleted
) {
    public static CachedUser from(UserEntity entity) {
        return new CachedUser(
            entity.getId(),
            entity.getName(),
            entity.getEmail(),
            entity.getHandle(),
            entity.getBio(),
            entity.getProfileImageUrl(),
            entity.isEmailVerified(),
            entity.getDeletedAt() != null
        );
    }
}

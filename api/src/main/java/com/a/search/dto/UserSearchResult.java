package com.a.search.dto;

import com.a.entity.UserEntity;

public record UserSearchResult(
    Long id,
    String name,
    String handle,
    String profileImageUrl,
    boolean isFollowing
) {
    public static UserSearchResult from(UserEntity user, boolean isFollowing) {
        return new UserSearchResult(
            user.getId(),
            user.getName(),
            user.getHandle(),
            user.getProfileImageUrl(),
            isFollowing
        );
    }
}

package com.a.user.dto;

import com.a.entity.UserEntity;

public record UserProfileResponse(
    Long id,
    String name,
    String email,
    String handle,
    String bio,
    String profileImageUrl,
    long postCount,
    long followerCount,
    long followingCount,
    boolean isFollowing,
    boolean isOwnProfile
) {
    public static UserProfileResponse from(
        UserEntity user,
        long postCount,
        long followerCount,
        long followingCount,
        boolean isFollowing,
        boolean isOwnProfile
    ) {
        return new UserProfileResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getHandle(),
            user.getBio(),
            user.getProfileImageUrl(),
            postCount,
            followerCount,
            followingCount,
            isFollowing,
            isOwnProfile
        );
    }
}

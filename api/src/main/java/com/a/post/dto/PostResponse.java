package com.a.post.dto;

import com.a.entity.PostEntity;
import com.a.entity.PostVisibility;
import java.time.Instant;
import java.util.List;

public record PostResponse(
    Long id,
    Long userId,
    String authorName,
    String authorHandle,
    String authorProfileImageUrl,
    String content,
    String imageUrl,
    PostVisibility visibility,
    long likeCount,
    long commentCount,
    boolean liked,
    boolean bookmarked,
    boolean isFollowing,
    boolean isOwnPost,
    List<String> tags,
    Instant createdAt
) {
    public static PostResponse from(
        PostEntity entity,
        String authorName,
        String authorHandle,
        String authorProfileImageUrl,
        long likeCount,
        long commentCount,
        boolean liked,
        boolean bookmarked,
        boolean isFollowing,
        boolean isOwnPost,
        List<String> tags
    ) {
        return new PostResponse(
            entity.getId(),
            entity.getUserId(),
            authorName,
            authorHandle,
            authorProfileImageUrl,
            entity.getContent(),
            entity.getImageUrl(),
            entity.getVisibility(),
            likeCount,
            commentCount,
            liked,
            bookmarked,
            isFollowing,
            isOwnPost,
            tags,
            entity.getCreatedAt()
        );
    }
}

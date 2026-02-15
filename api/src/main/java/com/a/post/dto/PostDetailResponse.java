package com.a.post.dto;

import com.a.entity.PostEntity;
import com.a.entity.PostVisibility;
import java.time.Instant;
import java.util.List;

public record PostDetailResponse(
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
    boolean isOwner,
    List<String> tags,
    Instant createdAt
) {
    public static PostDetailResponse from(
        PostEntity entity,
        String authorName,
        String authorHandle,
        String authorProfileImageUrl,
        long likeCount,
        long commentCount,
        boolean liked,
        boolean bookmarked,
        boolean isOwner,
        List<String> tags
    ) {
        return new PostDetailResponse(
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
            isOwner,
            tags,
            entity.getCreatedAt()
        );
    }
}

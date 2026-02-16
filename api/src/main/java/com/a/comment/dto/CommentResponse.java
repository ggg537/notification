package com.a.comment.dto;

import com.a.entity.CommentEntity;
import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
    Long id,
    Long userId,
    String authorName,
    String authorHandle,
    String authorProfileImageUrl,
    String content,
    LocalDateTime createdAt,
    boolean isOwner,
    Long parentId,
    int depth,
    List<CommentResponse> replies
) {
    public static CommentResponse from(
        CommentEntity entity,
        String authorName,
        String authorHandle,
        String authorProfileImageUrl,
        boolean isOwner,
        List<CommentResponse> replies
    ) {
        return new CommentResponse(
            entity.getId(),
            entity.getUserId(),
            authorName,
            authorHandle,
            authorProfileImageUrl,
            entity.getContent(),
            entity.getCreatedAt(),
            isOwner,
            entity.getParentId(),
            entity.getDepth(),
            replies
        );
    }
}

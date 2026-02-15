package com.a.bookmark.dto;

import com.a.entity.BookmarkEntity;
import java.time.Instant;

public record BookmarkResponse(
    Long id,
    Long postId,
    Long collectionId,
    Instant createdAt
) {
    public static BookmarkResponse from(BookmarkEntity entity) {
        return new BookmarkResponse(
            entity.getId(),
            entity.getPostId(),
            entity.getCollectionId(),
            entity.getCreatedAt()
        );
    }
}

package com.a.bookmark.dto;

import com.a.entity.BookmarkCollectionEntity;
import java.time.Instant;

public record BookmarkCollectionResponse(
    Long id,
    String name,
    Instant createdAt
) {
    public static BookmarkCollectionResponse from(BookmarkCollectionEntity entity) {
        return new BookmarkCollectionResponse(
            entity.getId(),
            entity.getName(),
            entity.getCreatedAt()
        );
    }
}

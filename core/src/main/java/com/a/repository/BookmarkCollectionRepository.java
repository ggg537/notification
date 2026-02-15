package com.a.repository;

import com.a.entity.BookmarkCollectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookmarkCollectionRepository extends JpaRepository<BookmarkCollectionEntity, Long> {
    List<BookmarkCollectionEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    void deleteAllByUserId(Long userId);
}

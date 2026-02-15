package com.a.repository;

import com.a.entity.BookmarkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<BookmarkEntity, Long> {
    Optional<BookmarkEntity> findByUserIdAndPostId(Long userId, Long postId);
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    List<BookmarkEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    List<BookmarkEntity> findAllByCollectionIdOrderByCreatedAtDesc(Long collectionId);
    void deleteAllByUserId(Long userId);

    @Query("SELECT b.postId FROM BookmarkEntity b WHERE b.userId = :userId AND b.postId IN :postIds")
    List<Long> findBookmarkedPostIds(@Param("userId") Long userId, @Param("postIds") Collection<Long> postIds);
}

package com.a.repository;

import com.a.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CommentJpaRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findAllByPostIdOrderByCreatedAtAsc(Long postId);
    List<CommentEntity> findAllByPostIdAndParentIdIsNullOrderByCreatedAtAsc(Long postId);
    List<CommentEntity> findAllByParentIdOrderByCreatedAtAsc(Long parentId);
    long countByPostId(Long postId);
    void deleteAllByUserId(Long userId);

    @Query("SELECT c.postId, COUNT(c) FROM CommentEntity c WHERE c.postId IN :postIds GROUP BY c.postId")
    List<Object[]> countByPostIdIn(@Param("postIds") Collection<Long> postIds);
}

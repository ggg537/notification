package com.a.repository;

import com.a.entity.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {
    Optional<LikeEntity> findByPostIdAndUserId(Long postId, Long userId);
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    long countByPostId(Long postId);
    void deleteAllByUserId(Long userId);

    @Query("SELECT l.postId, COUNT(l) FROM LikeEntity l WHERE l.postId IN :postIds GROUP BY l.postId")
    List<Object[]> countByPostIdIn(@Param("postIds") Collection<Long> postIds);

    @Query("SELECT l.postId FROM LikeEntity l WHERE l.postId IN :postIds AND l.userId = :userId")
    List<Long> findLikedPostIds(@Param("postIds") Collection<Long> postIds, @Param("userId") Long userId);
}

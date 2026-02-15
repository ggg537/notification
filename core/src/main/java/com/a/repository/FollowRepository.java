package com.a.repository;

import com.a.entity.FollowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<FollowEntity, Long> {
    Optional<FollowEntity> findByFollowerIdAndFollowingId(Long followerId, Long followingId);
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);
    long countByFollowingId(Long followingId);
    long countByFollowerId(Long followerId);
    List<FollowEntity> findAllByFollowerId(Long followerId);
    void deleteAllByFollowerIdOrFollowingId(Long followerId, Long followingId);

    @Query("SELECT f.followingId FROM FollowEntity f WHERE f.followerId = :followerId AND f.followingId IN :followingIds")
    List<Long> findFollowingIds(@Param("followerId") Long followerId, @Param("followingIds") Collection<Long> followingIds);
}

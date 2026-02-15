package com.a.repository;

import com.a.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<PostEntity, Long>, PostRepositoryCustom {
    Page<PostEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<PostEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    long countByUserId(Long userId);
    void deleteAllByUserId(Long userId);
}

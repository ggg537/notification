package com.a.repository;

import com.a.entity.HashtagEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HashtagRepository extends JpaRepository<HashtagEntity, Long> {
    Optional<HashtagEntity> findByTag(String tag);
    List<HashtagEntity> findByTagIn(List<String> tags);
    List<HashtagEntity> findByTagContainingIgnoreCase(String keyword, Pageable pageable);
    List<HashtagEntity> findAllByOrderByPostCountDesc(Pageable pageable);
}

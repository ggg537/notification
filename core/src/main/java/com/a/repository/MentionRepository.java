package com.a.repository;

import com.a.entity.MentionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MentionRepository extends JpaRepository<MentionEntity, Long> {
    List<MentionEntity> findAllByPostId(Long postId);
    List<MentionEntity> findAllByMentionedUserId(Long mentionedUserId);
    void deleteAllByPostId(Long postId);
}

package com.a.repository;

import com.a.entity.PostHashtagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PostHashtagRepository extends JpaRepository<PostHashtagEntity, Long> {
    List<PostHashtagEntity> findAllByPostId(Long postId);
    List<PostHashtagEntity> findAllByPostIdIn(Collection<Long> postIds);
    List<PostHashtagEntity> findAllByHashtagId(Long hashtagId);
    void deleteAllByPostId(Long postId);
}

package com.a.client;

import com.a.domain.Comment;
import com.a.entity.CommentEntity;
import com.a.repository.CommentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentClient {

  private final CommentJpaRepository commentJpaRepository;

  public Comment getComment(Long id) {
    CommentEntity entity = commentJpaRepository.findById(id).orElse(null);
    if (entity == null) return null;
    return new Comment(entity.getId(), entity.getUserId(), entity.getContent(), entity.getCreatedAt());
  }

}

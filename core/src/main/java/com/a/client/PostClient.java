package com.a.client;

import com.a.domain.Post;
import com.a.entity.PostEntity;
import com.a.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostClient {

  private final PostRepository postRepository;

  public Post getPost(Long id) {
    PostEntity entity = postRepository.findById(id).orElse(null);
    if (entity == null) return null;
    return new Post(entity.getId(), entity.getUserId(), entity.getImageUrl(), entity.getContent());
  }

}

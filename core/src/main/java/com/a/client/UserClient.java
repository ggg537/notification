package com.a.client;

import com.a.domain.User;
import com.a.entity.UserEntity;
import com.a.repository.FollowRepository;
import com.a.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserClient {

  private final UserRepository userRepository;
  private final FollowRepository followRepository;

  public User getUser(Long id) {
    UserEntity entity = userRepository.findById(id).orElse(null);
    if (entity == null) return null;
    return new User(entity.getId(), entity.getName(), entity.getProfileImageUrl());
  }

  public Boolean getIsFollowing(Long followerId, Long followedId) {
    return followRepository.existsByFollowerIdAndFollowingId(followerId, followedId);
  }

}

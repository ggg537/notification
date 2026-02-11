package com.a.service.converter;

import com.a.client.UserClient;
import com.a.domain.FollowNotification;
import com.a.domain.User;
import com.a.service.dto.ConvertedFollowNotification;
import com.a.service.dto.ConvertedLikeNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FollowNotificationConverter {

  private final UserClient userClient;

  public ConvertedFollowNotification convert(FollowNotification notification) {

    User user = userClient.getUser(notification.getFollowerId());
    boolean isFollowing = userClient.getIsFollowing(notification.getUserId(), notification.getFollowerId());

    return new ConvertedFollowNotification(
        notification.getId(),
        notification.getType(),
        notification.getOccurredAt(),
        notification.getLastUpdatedAt(),
        user.getName(),
        user.getProfileImageUrl(),
        isFollowing
    );
  }

}

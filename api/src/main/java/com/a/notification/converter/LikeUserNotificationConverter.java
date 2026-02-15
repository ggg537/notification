package com.a.notification.converter;

import com.a.client.PostClient;
import com.a.client.UserClient;
import com.a.domain.LikeNotification;
import com.a.domain.Post;
import com.a.domain.User;
import com.a.notification.dto.ConvertedLikeNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LikeUserNotificationConverter {

  private final UserClient userClient;
  private final PostClient postClient;

  public ConvertedLikeNotification convert(LikeNotification notification) {
    User user = userClient.getUser(notification.getLikerIds().getFirst());
    Post post = postClient.getPost(notification.getPostId());

    return new ConvertedLikeNotification(
        notification.getId(),
        notification.getType(),
        notification.getOccurredAt(),
        notification.getLastUpdatedAt(),
        user.getName(),
        user.getProfileImageUrl(),
        notification.getLikerIds().size(),
        post.getImageUrl()
    );
  }
}

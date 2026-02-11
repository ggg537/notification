package com.a.service.converter;

import com.a.client.PostClient;
import com.a.client.UserClient;
import com.a.domain.CommentNotification;
import com.a.domain.Post;
import com.a.domain.User;
import com.a.service.dto.ConvertedCommentNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CommentUserNotificationConverter {

  private final UserClient userClient;
  private final PostClient postClient;

  public ConvertedCommentNotification convert(CommentNotification notification) {

    User user = userClient.getUser(notification.getWriterId());
    Post post = postClient.getPost(notification.getPostId());

    return new ConvertedCommentNotification(
        notification.getId(),
        notification.getType(),
        notification.getOccurredAt(),
        notification.getLastUpdatedAt(),
        user.getName(),
        user.getProfileImageUrl(),
        notification.getComment(),
        post.getImageUrl()
    );
  }

}

package com.a.notification.service;

import com.a.domain.CommentNotification;
import com.a.domain.FollowNotification;
import com.a.domain.LikeNotification;
import com.a.notification.converter.CommentUserNotificationConverter;
import com.a.notification.converter.FollowNotificationConverter;
import com.a.notification.converter.LikeUserNotificationConverter;
import com.a.notification.dto.ConvertedNotification;
import com.a.notification.dto.GetUserNotificationsResult;
import com.a.service.NotificationListService;
import com.a.service.dto.GetUserNotificationsByPivotResult;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GetUserNotificationService {

  private final NotificationListService listService;
  private final CommentUserNotificationConverter commentConverter;
  private final LikeUserNotificationConverter likeConverter;
  private final FollowNotificationConverter followConverter;

  public GetUserNotificationsResult getUserNotificationByPivot(long userId, LocalDateTime pivot) {
    GetUserNotificationsByPivotResult result = listService.getUserNotificationByPivot(userId, pivot);

    List<ConvertedNotification> convertedNotifications = result.getNotifications().stream()
        .map(notification -> switch (notification.getType()) {
          case COMMENT -> commentConverter.convert((CommentNotification) notification);
          case LIKE -> likeConverter.convert((LikeNotification) notification);
          case FOLLOW -> followConverter.convert((FollowNotification) notification);
        })
        .toList();

    return new GetUserNotificationsResult(
        convertedNotifications,
        result.isHasNext()
    );
  }
}

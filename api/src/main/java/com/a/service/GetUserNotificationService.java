package com.a.service;

import com.a.domain.CommentNotification;
import com.a.domain.FollowNotification;
import com.a.domain.LikeNotification;
import com.a.service.converter.CommentUserNotificationConverter;
import com.a.service.converter.FollowNotificationConverter;
import com.a.service.converter.LikeUserNotificationConverter;
import com.a.service.dto.ConvertedNotification;
import com.a.service.dto.GetUserNotificationsByPivotResult;
import com.a.service.dto.GetUserNotificationsResult;
import java.time.Instant;
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

  public GetUserNotificationsResult getUserNotificationByPivot(long userId, Instant pivot){

    // 알림 목록 조회
    GetUserNotificationsByPivotResult result = listService.getUserNotificationByPivot(userId, pivot);

    // 알림 목록을 순회하면서 DB 알림 -> 사용자 알림으로 변환
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

package com.a.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CheckNewNotificationService {

  private final NotificationGetService notificationGetService;
  private final LastReadAtService lastReadAtService;

  // 신규 알림 확인
  public boolean checkNewNotification(long userId)  {

    Instant latestUpdatedAt = notificationGetService.getLatestUpdatedAt(userId);
    if(latestUpdatedAt == null) {
      return false;
    }

    Instant lastReadAt = lastReadAtService.getLastReadAt(userId);
    if(lastReadAt == null) {
      return true;
    }
    return latestUpdatedAt.isAfter(lastReadAt);
  }
}

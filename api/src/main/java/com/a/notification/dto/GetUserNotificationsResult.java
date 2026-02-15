package com.a.notification.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetUserNotificationsResult {

  private List<ConvertedNotification> notifications;
  private boolean hasNext;
}

package com.a.notification.controller;

import com.a.notification.dto.UserNotificationListResponse;
import com.a.notification.service.GetUserNotificationService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/user-notifications")
public class UserNotificationListController implements UserNotificationListControllerSpec {

  private final GetUserNotificationService getUserNotificationService;

  @Override
  @GetMapping("/{userId}")
  public UserNotificationListResponse getNotifications(
      @PathVariable(value = "userId") Long userId,
      @RequestParam(value = "pivot", required = false) Instant pivot
  ) {
    return UserNotificationListResponse.of(
        getUserNotificationService.getUserNotificationByPivot(userId, pivot)
    );
  }
}

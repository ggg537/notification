package com.a.notification.controller;

import com.a.notification.dto.CheckNewNotificationResponse;
import com.a.notification.service.CheckNewNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/user-notifications")
public class CheckNewNotificationController implements CheckNewNotificationControllerSpec {

  private final CheckNewNotificationService service;

  @Override
  @GetMapping("/{userId}/new")
  public CheckNewNotificationResponse checkNew(@PathVariable(value = "userId") Long userId) {
    boolean hasNew = service.checkNewNotification(userId);
    return new CheckNewNotificationResponse(hasNew);
  }
}

package com.a.task;

import com.a.service.NotificationGetService;
import com.a.utils.NotificationIdGenerator;
import com.a.service.NotificationSaveService;
import com.a.client.PostClient;
import com.a.domain.FollowNotification;
import com.a.domain.NotificationType;
import com.a.event.FollowEvent;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class FollowAddTask {

  private final NotificationSaveService saveService;

  public void processEvent(FollowEvent event) {
    saveService.insert(createFollowNotification(event));
  }

  private FollowNotification createFollowNotification(FollowEvent event) {

    Instant now = Instant.now();

    return new FollowNotification(
        NotificationIdGenerator.generate(),
        event.getTargetUserId(),
        NotificationType.FOLLOW,
        event.getCreatedAt(),
        now,
        now,
        now.plus(90, ChronoUnit.DAYS),
        event.getUserId()
    );
  }
}

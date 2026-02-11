package com.a.task;

import static com.a.domain.NotificationType.LIKE;
import com.a.service.NotificationGetService;
import com.a.service.NotificationRemoveService;
import com.a.service.NotificationSaveService;
import com.a.client.PostClient;
import com.a.domain.LikeNotification;
import com.a.domain.Notification;
import com.a.event.LikeEvent;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// 좋아요 삭제 이벤트

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeRemoveTask {

  private final NotificationRemoveService removeService;
  private final NotificationGetService getService;
  private final NotificationSaveService saveService;

  // 이벤트를 받아서 처리하는 함수
  public void processEvent(LikeEvent event) {
    Optional<Notification> optionalNotification = getService.getNotificationByTypeAndPostId(LIKE, event.getPostId());
    if (optionalNotification.isEmpty()) {
      log.error("No notification with postId: {}", event.getPostId());
      return;
    }
    // likers 에서 event.userId 제거 1. users 가 비어있으면 알림 삭제. 2 남아 있으면 알림 업데이트
    LikeNotification notification = (LikeNotification) optionalNotification.get();
    removeLikerAndUpdateNotification(notification, event);
  }

  private void removeLikerAndUpdateNotification(LikeNotification notification, LikeEvent event){

    notification.removeLiker(event.getUserId(), Instant.now());

    if (notification.getLikerIds().isEmpty()) {
      removeService.deleteById(notification.getId());
    } else {
      saveService.upsert(notification);
    }

  }

}

package com.a.task;

import static com.a.domain.NotificationType.LIKE;

import com.a.service.NotificationGetService;
import com.a.utils.NotificationIdGenerator;
import com.a.service.NotificationSaveService;
import com.a.domain.Post;
import com.a.client.PostClient;
import com.a.domain.LikeNotification;
import com.a.domain.Notification;
import com.a.event.LikeEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeAddTask {

  private final PostClient postClient;
  private final NotificationGetService getService;
  private final NotificationSaveService saveService;

  public void processEvent(LikeEvent event){

    Post post = postClient.getPost(event.getPostId());
    if(post == null){
      log.error("Post is null with postId:{}", event.getPostId());
      return;
    }

    if (post.getUserId().equals(event.getUserId())) {
      return;
    }

    // likeNotification 1. 신규 생성 2. 업데이트 3. db 저장
    saveService.upsert(createOrUpdateNotification(post, event));
  }

  private Notification createOrUpdateNotification(Post post, LikeEvent event) {
    Optional<Notification> optionalNotification = getService.getNotificationByTypeAndPostId(LIKE, post.getId());

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime retention = now.plusDays(90);

    if (optionalNotification.isPresent()) {
      // 업데이트
      return updateNotification((LikeNotification) optionalNotification.get(), event, now, retention);


    } else {
      // 신규 생성
      return createNotification(post, event, now, retention);
    }
  }

  private Notification updateNotification(LikeNotification notification, LikeEvent event, LocalDateTime now, LocalDateTime retention){
    notification.addLiker(event.getUserId(), event.getCreatedAt(), now, retention);
    return notification;
  }

  private Notification createNotification(Post post, LikeEvent event, LocalDateTime now, LocalDateTime retention){
    return new LikeNotification(
        NotificationIdGenerator.generate(),
        post.getUserId(),
        LIKE,
        event.getCreatedAt(),   // 이벤트가 발생한 시점
        now,
        now,
        retention,
        post.getId(),
        List.of(event.getUserId())
    );
  }
}

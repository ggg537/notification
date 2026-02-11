package com.a.task;

import com.a.service.NotificationGetService;
import com.a.service.NotificationRemoveService;
import com.a.domain.Post;
import com.a.client.PostClient;
import com.a.domain.NotificationType;
import com.a.event.CommentEvent;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentRemoveTask {

  private final PostClient postClient;
  private final NotificationGetService getService;
  private final NotificationRemoveService removeService;

  // 이벤트를 받아서 처리하는 함수
  public void processEvent(CommentEvent event){

    // 내가 작성한 댓글인 경우 무시
    Post post = postClient.getPost(event.getPostId());
    if(Objects.equals(post.getUserId(), event.getUserId())) {
      return;
    }
    getService.getNotificationByTypeAndCommentId(NotificationType.COMMENT,
        event.getCommentId())
        .ifPresentOrElse(
            notification -> removeService.deleteById(notification.getId()),
                () -> log.error("Notification not found")
        );
  }
}

package com.a.task;

import com.a.domain.Comment;
import com.a.client.CommentClient;
import com.a.utils.NotificationIdGenerator;
import com.a.service.NotificationSaveService;
import com.a.domain.Post;
import com.a.client.PostClient;
import com.a.domain.CommentNotification;
import com.a.domain.Notification;
import com.a.domain.NotificationType;
import com.a.event.CommentEvent;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentAddTask {

  private final PostClient postClient;
  private final CommentClient commentClient;
  private final NotificationSaveService saveService;

  // 이벤트를 받아서 처리하는 함수
  public void processEvent(CommentEvent event){

    // 내가 작성한 댓글인 경우 무시
    Post post = postClient.getPost(event.getPostId());
    if(Objects.equals(post.getUserId(), event.getUserId())) {
      return;
    }
    Comment comment = commentClient.getComment(event.getCommentId());

    // 알림 생성
    Notification notification = createNotification(post, comment);

    // 저장
    saveService.insert(notification);

  }

  // 댓글 알림을 생성하는 함수
  private Notification createNotification(Post post, Comment comment){
    Instant now = Instant.now();

    return new CommentNotification(
        NotificationIdGenerator.generate(),
        post.getUserId(),
        NotificationType.COMMENT,
        comment.getCreatedAt(),
        now,
        now,
        now.plus(90, ChronoUnit.DAYS),
        post.getId(),
        comment.getUserId(),
        comment.getContent(),
        comment.getId()
    );
  }

}

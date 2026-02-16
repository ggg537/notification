package com.a.repository;

import com.a.IntegrationTest;
import com.a.domain.CommentNotification;
import com.a.domain.Notification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.a.domain.NotificationType.COMMENT;
import static org.junit.jupiter.api.Assertions.*;

//@SpringBootTest
//@SpringBootApplication
class NotificationRepositoryIntegrationTest extends IntegrationTest {

  @Autowired
  private NotificationRepository sut;

  private final long userId = 2L;
  private final long postId = 3L;
  private final long writerId = 4L;
  private final long commentId = 5L;
  private final String comment = "comment";
  private final LocalDateTime now = LocalDateTime.now();
  private final LocalDateTime ninetyDaysAfter = LocalDateTime.now().plusDays(90);

  @BeforeEach
  void setUp() {
    for (int i = 1; i <= 5; i++) {
      LocalDateTime occurredAt = now.minusDays(i);
      sut.save(new CommentNotification("id-" + i, userId, COMMENT, occurredAt,
          now, now, ninetyDaysAfter, postId, writerId, comment, commentId));
    }
  }

  @AfterEach
  void tearDown() {
    sut.deleteAll();
  }

  // 알림 저장
  @Test
  void testSave() {

    // 알림 객체 생성 및 저장
    String id = "1";
    sut.save(createCommentNotification(id));

    // 조회 시 객체가 있나?
    Optional<Notification> optionalNotification = sut.findById(id);

    assertTrue(optionalNotification.isPresent());
  }

  @Test
  void testFindById() {
    String id = "2";

    sut.save(createCommentNotification(id));
    Optional<Notification> optionalNotification = sut.findById(id);

    CommentNotification notification = (CommentNotification) optionalNotification.orElseThrow();
    assertEquals(notification.getId(), id);
    assertEquals(notification.getUserId(), userId);
    assertEquals(notification.getOccurredAt().withNano(0), now.withNano(0));
    assertEquals(notification.getCreatedAt().withNano(0), now.withNano(0));
    assertEquals(notification.getLastUpdatedAt().withNano(0), now.withNano(0));
    assertEquals(notification.getDeletedAt().withNano(0), ninetyDaysAfter.withNano(0));
    assertEquals(notification.getPostId(), postId);
    assertEquals(notification.getWriterId(), writerId);
    assertEquals(notification.getComment(), comment);
    assertEquals(notification.getCommentId(), commentId);
  }

  @Test
  void testDeleteById() {
    String id = "3";

    sut.save(createCommentNotification(id));
    sut.deleteById(id);
    Optional<Notification> optionalNotification = sut.findById(id);

    assertFalse(optionalNotification.isPresent());
  }

  @Test
  void testFindAllByUserIdOrderByOccurredAtDesc() {
    Pageable pageable = PageRequest.of(0, 3);

    Slice<Notification> result = sut.findAllByUserIdOrderByOccurredAtDesc(userId, pageable);

    assertEquals(3, result.getContent().size());
    assertTrue(result.hasNext());

    Notification first = result.getContent().get(0);
    Notification second = result.getContent().get(1);
    Notification third = result.getContent().get(2);

    assertTrue(first.getOccurredAt().isAfter(second.getOccurredAt()));
    assertTrue(second.getOccurredAt().isAfter(third.getOccurredAt()));
  }

  @Test
  void testFindAllByUserIdAndOccurredAtLessThanOrderByOccurredAtDescFirstQuery() {
    Pageable pageable = PageRequest.of(0, 3);

    Slice<Notification> result = sut.findAllByUserIdAndOccurredAtLessThanOrderByOccurredAtDesc(userId, now, pageable);

    assertEquals(3, result.getContent().size());
    assertTrue(result.hasNext());

    Notification first = result.getContent().get(0);
    Notification second = result.getContent().get(1);
    Notification third = result.getContent().get(2);

    assertTrue(first.getOccurredAt().isAfter(second.getOccurredAt()));
    assertTrue(second.getOccurredAt().isAfter(third.getOccurredAt()));
  }

  @Test
  void testFindAllByUserIdAndOccurredAtLessThanOrderByOccurredAtDescSecondQueryWithPivot() {
    Pageable pageable = PageRequest.of(0, 3);

    Slice<Notification> firstResult = sut.findAllByUserIdAndOccurredAtLessThanOrderByOccurredAtDesc(userId, now, pageable);
    Notification last = firstResult.getContent().get(2);

    LocalDateTime pivot = last.getOccurredAt();
    Slice<Notification> secondResult = sut.findAllByUserIdAndOccurredAtLessThanOrderByOccurredAtDesc(userId, pivot, pageable);

    assertEquals(2, secondResult.getContent().size());
    assertFalse(secondResult.hasNext());

    Notification first = secondResult.getContent().get(0);
    Notification second = secondResult.getContent().get(1);

    assertTrue(first.getOccurredAt().isAfter(second.getOccurredAt()));
  }

  private CommentNotification createCommentNotification(String id) {
    return new CommentNotification(id, userId, COMMENT, now, now, now, ninetyDaysAfter, postId, writerId, comment, commentId);
  }
}
package com.a.service;

import com.a.domain.Notification;
import com.a.repository.NotificationRepository;
import com.a.service.dto.GetUserNotificationsByPivotResult;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class NotificationListService {

  private final NotificationRepository repository;

  private static final int PAGE_SIZE = 20;

  // 목록 조회 : pivot 방식 (기준점: occurredAt) vs Paging 방식(Page 방식)
  public GetUserNotificationsByPivotResult getUserNotificationByPivot(long userId, LocalDateTime occurredAt) {

    Slice<Notification> result;

    if (occurredAt == null){
      result = repository.findAllByUserIdOrderByOccurredAtDesc(userId, PageRequest.of(0, PAGE_SIZE));
    } else {
      result = repository.findAllByUserIdAndOccurredAtLessThanOrderByOccurredAtDesc(userId, occurredAt, PageRequest.of(0, PAGE_SIZE));
    }

    return new GetUserNotificationsByPivotResult(
        result.toList(),
        result.hasNext()
    );
  }
}

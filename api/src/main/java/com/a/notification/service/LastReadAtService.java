package com.a.notification.service;

import com.a.repository.NotificationReadRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LastReadAtService {

  private final NotificationReadRepository repository;

  public LocalDateTime setLastReadAt(long userId) {
    return repository.setLastReadAt(userId);
  }

  public LocalDateTime getLastReadAt(long userId) {
    return repository.getLastReadAt(userId);
  }
}

package com.a.service;

import com.a.repository.NotificationReadRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LastReadAtService {

  private final NotificationReadRepository repository;

  public Instant setLastReadAt(long userId) {
    return repository.setLastReadAt(userId);
  }

  public Instant getLastReadAt(long userId) {
    return repository.getLastReadAt(userId);
  }

}

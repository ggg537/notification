package com.a.service;

import com.a.repository.NotificationRepository;
import com.a.domain.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class NotificationSaveService {

  private final NotificationRepository repository;

  // insert : 기존 id가 있으면 오류 생성
  public void insert(Notification notification){
    Notification result = repository.insert(notification);
    log.info("inserted: {} ", result);
  }

  // upsert = update + insert
  // save = 기존 id 있으면 update, 없으면 save
  public void upsert(Notification notification) {
    Notification result = repository.save(notification);
    log.info("upserted: {} ", result);
  }

}

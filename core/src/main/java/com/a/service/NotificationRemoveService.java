package com.a.service;

import com.a.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class NotificationRemoveService {

  private final NotificationRepository repository;

  public void deleteById(String id){
    log.info("deleted : {} ", id);
    repository.deleteById(id);
  }

}

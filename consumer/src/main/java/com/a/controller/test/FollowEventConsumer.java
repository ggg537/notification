package com.a.controller.test;

import static com.a.event.FollowEventType.ADD;
import static com.a.event.FollowEventType.REMOVE;

import com.a.event.FollowEvent;
import com.a.task.FollowAddTask;
import com.a.task.FollowRemoveTask;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class FollowEventConsumer {

  private final FollowAddTask followAddTask;
  private final FollowRemoveTask followRemoveTask;

  @Bean("follow")
  public Consumer<FollowEvent> follow(){
    return event -> {
      if (event.getType() == ADD) {
        followAddTask.processEvent(event);
      } else if (event.getType() == REMOVE) {
        followRemoveTask.processEvent(event);
      }
    };
  }

}

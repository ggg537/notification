package com.a.controller.test;

import static com.a.event.CommentEventType.ADD;
import static com.a.event.CommentEventType.REMOVE;

import com.a.event.CommentEvent;
import com.a.task.CommentAddTask;
import com.a.task.CommentRemoveTask;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentEventConsumer {

  private final CommentAddTask commentAddTask;
  private final CommentRemoveTask commentRemoveTask;

  // CommentEventType.ADD 이면 댓글 추가 이벤트 호출
  @Bean("comment")
  public Consumer<CommentEvent> comment(){
    return event -> {
      if(event.getType() == ADD){
        commentAddTask.processEvent(event);
      } else if (event.getType() == REMOVE) {
        commentRemoveTask.processEvent(event);
      }
    };

  }

}

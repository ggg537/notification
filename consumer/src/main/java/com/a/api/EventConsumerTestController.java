package com.a.api;

import com.a.event.CommentEvent;
import com.a.event.FollowEvent;
import com.a.event.LikeEvent;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class EventConsumerTestController implements EventConsumerTestControllerSpec{

  private final Consumer<CommentEvent> comment;
  private final Consumer<LikeEvent> like;
  private final Consumer<FollowEvent> follow;

  @Override
  @PostMapping("/test/comment")
  public void comment(@RequestBody CommentEvent event){
    comment.accept(event);
  }

  @Override
  @PostMapping("/test/like")
  public void like(@RequestBody LikeEvent event){
    like.accept(event);
  }

  @Override
  @PostMapping("/test/follow")
  public void follow(@RequestBody FollowEvent event){
    follow.accept(event);
  }
}

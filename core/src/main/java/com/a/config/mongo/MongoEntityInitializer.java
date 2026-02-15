package com.a.config.mongo;

import com.a.domain.CommentNotification;
import com.a.domain.FollowNotification;
import com.a.domain.LikeNotification;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration
@RequiredArgsConstructor
public class MongoEntityInitializer {

    private final MongoMappingContext mongoMappingContext;

    @PostConstruct
    public void initMappings() {
        mongoMappingContext.getPersistentEntity(CommentNotification.class);
        mongoMappingContext.getPersistentEntity(LikeNotification.class);
        mongoMappingContext.getPersistentEntity(FollowNotification.class);
    }
}

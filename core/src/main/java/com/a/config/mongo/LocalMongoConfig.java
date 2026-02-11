package com.a.config.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@Profile("test")
@Slf4j
@Configuration
public class LocalMongoConfig {

  private static final String MONGODB_IMAGE_NAME = "mongo:5.0";
  private static final String DATABASE_NAME = "notification";
  private static final int MONGODB_INNER_PORT = 27017;
  private static final GenericContainer mongo = createMongoInstance();

  // 몽고DB 생성 컨테이너를 생성하는 함수
  private static GenericContainer createMongoInstance(){
    return new GenericContainer(DockerImageName.parse(MONGODB_IMAGE_NAME))
        .withExposedPorts(MONGODB_INNER_PORT)
        .withReuse(true);
  }

  // 시작할 때
  @PostConstruct
  public void StartMongo(){
    try {
      mongo.start();
    } catch (Exception e){
      log.error(e.getMessage());
    }
  }

  // 종료될 때
  @PreDestroy
  public void StopMongo(){
    try {
      mongo.stop();
    } catch (Exception e){
      log.error(e.getMessage());
    }
  }

  // 몽고DB 연결을 도와주는 Factory
  @Bean(name = "notificationMongoFactory")
  public MongoDatabaseFactory notificationMongoFactory(){
    return new SimpleMongoClientDatabaseFactory(connectionString());
  }

  private ConnectionString connectionString() {
    String host = mongo.getHost();
    Integer port = mongo.getMappedPort(MONGODB_INNER_PORT);
    log.info("MongoDB Connection String: mongodb://{}:{}/{}", host, port, DATABASE_NAME);
    return new ConnectionString("mongodb://" + host + ":" + port + "/" + DATABASE_NAME);
  }
}

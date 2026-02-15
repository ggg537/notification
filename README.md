# 🔔 SNS Platform

> **실시간 알림 시스템을 갖춘 소셜 네트워크 서비스**
>
> Spring Boot 3 기반 멀티 모듈 아키텍처 · Kafka 이벤트 · Redis 캐싱 · MongoDB 알림 저장소

<br/>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 3"/>
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL"/>
  <img src="https://img.shields.io/badge/MongoDB-5.0-47A248?style=for-the-badge&logo=mongodb&logoColor=white" alt="MongoDB"/>
  <img src="https://img.shields.io/badge/Redis-7.4-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="Redis"/>
  <img src="https://img.shields.io/badge/Kafka-3.x-231F20?style=for-the-badge&logo=apachekafka&logoColor=white" alt="Kafka"/>
  <img src="https://img.shields.io/badge/QueryDSL-5.1-0769AD?style=for-the-badge" alt="QueryDSL"/>
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker"/>
</p>

<br/>

## 📌 프로젝트 소개

SNS는 게시글 작성, 팔로우, 좋아요, 댓글, 북마크, 실시간 알림 등 소셜 네트워크의 핵심 기능을 구현한 프로젝트입니다.

단순한 CRUD를 넘어 **이벤트 기반 아키텍처**, **분산 락**, **다중 데이터소스 전략**, **MSA-Ready 설계**를 적용하여 실무 수준의 백엔드 설계 역량을 보여주는 것을 목표로 하였습니다.

**핵심 목표**

- 🏗️ **MSA 전환을 고려한 모듈 분리** — Entity 간 ID 참조, Client 패턴, 이벤트 기반 통신
- ⚡ **Redis를 활용한 다층 캐싱 전략** — 카운트, 피드, 유저, 세션, 분산 락, 인기 검색어
- 📨 **Kafka 기반 비동기 알림 처리** — API ↔ Consumer 모듈 간 느슨한 결합
- 🔐 **JWT + OAuth2 인증 시스템** — 이중 저장소(MySQL + Redis) 토큰 관리

<br/>

---

## 🏛️ 시스템 아키텍처

```mermaid
graph TB
    subgraph Client
        UI[Thymeleaf + Vanilla JS]
    end

    subgraph API_Module
        direction TB
        AC[Auth Controller]
        PC[Post Controller]
        NC[Notification Controller]
        SEC[Security Filter - JWT]
        SVC[Services]
    end

    subgraph Consumer_Module
        direction TB
        KC[Kafka Consumers]
        CT[Comment Task]
        LT[Like Task]
        FT[Follow Task]
    end

    subgraph Infrastructure
        MySQL[(MySQL 8.0)]
        MongoDB[(MongoDB 5.0)]
        Redis[(Redis 7.4)]
        Kafka[[Apache Kafka]]
    end

    UI -->|HTTP / REST| API_Module
    SEC -->|JWT 검증| Redis
    API_Module -->|JPA / QueryDSL| MySQL
    API_Module -->|이벤트 발행| Kafka
    API_Module -->|캐시 / 락 / 세션| Redis
    API_Module -->|알림 조회| MongoDB
    Kafka -->|이벤트 소비| Consumer_Module
    Consumer_Module -->|알림 저장| MongoDB
    Consumer_Module -->|게시글/유저 조회| MySQL
```

<br/>

---

## 📦 모듈 구조

```mermaid
graph LR
    subgraph core[Core Module]
        E[Entity]
        R[Repository]
        D[Domain - Notification]
        EV[Event - Kafka DTO]
        CL[Client]
        CF[Config]
    end

    subgraph api[API Module - port 8080]
        CTRL[Controllers - domains]
        SVC2[Services]
        DTO[DTOs - Java Record]
        AUTH[Security - JWT / OAuth2]
        FE[Frontend - HTML / JS / CSS]
    end

    subgraph consumer[Consumer Module - port 8081]
        CONS[Kafka Consumers]
        TASK[Event Tasks]
        NS[Notification Services]
    end

    api -->|의존| core
    consumer -->|의존| core
```

```
sns-platform/
├── api/                          # API 모듈 (port 8080)
│   └── src/main/
│       ├── java/com/a/
│       │   ├── auth/             # 인증 (JWT)
│       │   ├── post/             # 게시글 CRUD, 피드
│       │   ├── comment/          # 댓글, 대댓글
│       │   ├── like/             # 좋아요 토글
│       │   ├── follow/           # 팔로우 토글
│       │   ├── bookmark/         # 북마크, 컬렉션
│       │   ├── search/           # 통합 검색, 트렌딩
│       │   ├── notification/     # 알림 조회 API
│       │   ├── user/             # 프로필, 계정 관리
│       │   ├── hashtag/          # 해시태그, 멘션
│       │   └── common/           # 보안, 예외, 이메일
│       └── resources/
│           ├── templates/        # Thymeleaf
│           └── static/           # JS, CSS
│
├── consumer/                     # Consumer 모듈 (port 8081)
│   └── src/main/java/com/a/
│       ├── consumer/             # Kafka 이벤트 소비자
│       └── task/                 # 알림 생성/삭제 태스크
│
├── core/                         # 공유 모듈
│   └── src/main/java/com/a/
│       ├── entity/               # JPA Entity
│       ├── repository/           # JPA + Redis + Mongo
│       ├── domain/               # MongoDB 도메인
│       ├── event/                # Kafka 이벤트 DTO
│       ├── client/               # 모듈 간 호출 Client
│       ├── config/               # Redis, Mongo, JPA, QueryDSL
│       └── service/              # 알림 저장/조회/삭제
│
└── docker-compose.yml            # MySQL, MongoDB, Redis, Kafka
```

<br/>

---

## ⚡ 주요 기능

### 인증 & 계정

| 기능 | 설명 |
|------|------|
| JWT 인증 | Access Token(15분) + Refresh Token(7일) 이중 토큰 |
| 토큰 이중 저장 | Redis(빠른 조회) + MySQL(영속성) 이중 기록 |
| 이메일 인증 | 회원가입 시 인증 메일 발송, 토큰 기반 검증 |
| 비밀번호 재설정 | 이메일 기반 비밀번호 초기화 플로우 |
| 계정 삭제 | Soft Delete + 연관 데이터 캐스케이딩 정리 |

### 게시글 & 피드

| 기능 | 설명 |
|------|------|
| 피드 탭 | 전체 / 팔로잉 / 인기 3가지 피드 |
| 게시글 공개 범위 | PUBLIC / FOLLOWERS_ONLY / PRIVATE |
| 이미지 업로드 | Multipart 파일 업로드, UUID 기반 파일명 |
| 해시태그 | `#태그` 자동 추출, 해시태그별 검색, 트렌딩 |
| 멘션 | `@핸들` 자동 추출, 사용자 연결 |
| 인기 게시글 | 좋아요 수 기반 QueryDSL LEFT JOIN 정렬 |

### 소셜 기능

| 기능 | 설명 |
|------|------|
| 좋아요 토글 | 분산 락 → DB 토글 → Redis 카운트 → Kafka 이벤트 |
| 팔로우 토글 | 동일 패턴. 자기 자신 팔로우 차단 |
| 댓글 / 대댓글 | 2단계 깊이 제한, 부모 댓글 삭제 시 대댓글 연쇄 삭제 |
| 북마크 컬렉션 | 북마크 토글 + 사용자 정의 컬렉션 관리 |
| 온라인 상태 | Redis TTL 기반 실시간 접속 표시 |

### 알림 시스템

| 기능 | 설명 |
|------|------|
| 이벤트  | API → Kafka → Consumer → MongoDB |
| 알림 유형 | 좋아요, 댓글, 팔로우 3종 |
| 좋아요 알림 집계 | "A님 외 3명이 좋아합니다" 패턴 (likerIds 리스트) |
| 새 알림 확인 | lastReadAt 기반 미읽음 판별, 폴링 |
| Pivot 페이징 | occurredAt 기반 커서 페이지네이션 |

### 검색

| 기능 | 설명 |
|------|------|
| 통합 검색 | 게시글 / 사용자 / 해시태그 탭 분리 |
| 해시태그 검색 | `#`으로 시작하면 해시태그 기반 조회 |
| 해시태그 | postCount 기반 인기 해시태그 |
| 인기 검색어 | Redis Sorted Set 기반 일별 검색어 랭킹 |

<br/>

---

## 🔄 이벤트 흐름 상세

### 좋아요 알림 플로우

```mermaid
sequenceDiagram
    participant U as 사용자
    participant API as API Module
    participant Lock as Redis Lock
    participant DB as MySQL
    participant Cache as Redis Cache
    participant K as Kafka
    participant C as Consumer
    participant M as MongoDB

    U->>API: POST /api/likes/{postId}
    API->>Lock: tryLock(userId, like, postId)
    Lock-->>API: 락 획득

    API->>DB: findByPostIdAndUserId()

    alt 좋아요 없음
        API->>DB: save(LikeEntity)
        API->>Cache: increment(like_count)
        API->>K: send(like, ADD event)
    else 좋아요 있음
        API->>DB: delete(LikeEntity)
        API->>Cache: decrement(like_count)
        API->>K: send(like, REMOVE event)
    end

    API->>Lock: unlock()
    API-->>U: liked, count

    K->>C: LikeEvent 소비

    alt ADD
        C->>M: 기존 알림 조회
        alt 알림 존재
            C->>M: likerIds에 추가 upsert
        else 알림 없음
            C->>M: 새 LikeNotification 생성
        end
    else REMOVE
        C->>M: likerIds에서 제거
        alt likerIds 비어있음
            C->>M: 알림 삭제
        end
    end
```

### 피드 조회 플로우

```mermaid
sequenceDiagram
    participant U as 사용자
    participant API as API Module
    participant Cache as Redis
    participant DB as MySQL
    participant QD as QueryDSL

    U->>API: GET /api/posts?tab=all&page=0
    API->>DB: findAllByFollowerId(userId)
    Note over API: 팔로잉 목록 조회

    API->>QD: findFeedPosts(userId, followingIds)
    Note over QD: 내 글 전체<br/>팔로잉 PUBLIC FOLLOWERS_ONLY<br/>기타 PUBLIC만
    QD-->>API: Page of PostEntity

    loop 게시글마다
        API->>Cache: getCount(like_count)
        alt 캐시 히트
            Cache-->>API: count
        else 캐시 미스
            API->>DB: countByPostId()
            API->>Cache: setCount(key, count)
        end
    end

    API-->>U: PostPageResponse
```

<br/>

---

## 🗂️ ERD

```mermaid
erDiagram
    users {
        bigint id PK
        varchar email UK
        varchar password
        varchar name
        varchar handle UK
        varchar bio
        varchar profile_image_url
        boolean email_verified
        varchar oauth_provider
        varchar oauth_provider_id
        timestamp deleted_at
        timestamp created_at
        timestamp updated_at
    }

    posts {
        bigint id PK
        bigint user_id FK
        text content
        varchar image_url
        enum visibility
        timestamp created_at
        timestamp updated_at
    }

    comments {
        bigint id PK
        bigint post_id FK
        bigint user_id FK
        text content
        bigint parent_id FK
        int depth
        timestamp created_at
    }

    likes {
        bigint id PK
        bigint post_id FK
        bigint user_id FK
        timestamp created_at
    }

    follows {
        bigint id PK
        bigint follower_id FK
        bigint following_id FK
        timestamp created_at
    }

    bookmarks {
        bigint id PK
        bigint user_id FK
        bigint post_id FK
        bigint collection_id FK
        timestamp created_at
    }

    bookmark_collections {
        bigint id PK
        bigint user_id FK
        varchar name
        timestamp created_at
    }

    hashtags {
        bigint id PK
        varchar tag UK
        bigint post_count
    }

    post_hashtags {
        bigint id PK
        bigint post_id FK
        bigint hashtag_id FK
    }

    mentions {
        bigint id PK
        bigint post_id FK
        bigint mentioned_user_id FK
        bigint mentioner_user_id FK
        timestamp created_at
    }

    refresh_tokens {
        bigint id PK
        bigint user_id FK
        varchar token UK
        timestamp expires_at
        timestamp created_at
    }

    users ||--o{ posts : "작성"
    users ||--o{ comments : "작성"
    users ||--o{ likes : "좋아요"
    users ||--o{ follows : "팔로워"
    users ||--o{ bookmarks : "북마크"
    users ||--o{ bookmark_collections : "소유"
    users ||--o{ mentions : "멘션됨"
    users ||--o{ refresh_tokens : "인증"
    posts ||--o{ comments : "포함"
    posts ||--o{ likes : "받음"
    posts ||--o{ bookmarks : "북마크됨"
    posts ||--o{ post_hashtags : "태그"
    posts ||--o{ mentions : "멘션포함"
    hashtags ||--o{ post_hashtags : "연결"
    bookmark_collections ||--o{ bookmarks : "분류"
    comments ||--o{ comments : "대댓글"
```

<br/>

---

## 🔧 Redis 활용 전략

```mermaid
graph LR
    subgraph CountCache[Count Cache - TTL 1h]
        CC1[like_count:postId]
        CC2[comment_count:postId]
        CC3[follow_followers:userId]
        CC4[follow_following:userId]
    end

    subgraph FeedCache[Feed Cache - TTL 30s]
        FC1["feed:userId:tab:page"]
    end

    subgraph UserCache[User Cache - TTL 5m]
        UC1["user:userId"]
    end

    subgraph DistLock[Distributed Lock - TTL 2s]
        DL1["lock:userId:action:targetId"]
    end

    subgraph TokenStore[Token Store - TTL 7d]
        TS1["refresh_token:token"]
        TS2["refresh_user:userId"]
    end

    subgraph OnlineStatus[Online Status - TTL 60s]
        OS1["online:userId"]
    end

    subgraph SearchRanking[Search Ranking - TTL 7d]
        SR1["search_popular:date"]
    end

    subgraph NotifRead[Notification Read - TTL 90d]
        NR1["notification_last_read:userId"]
    end
```

| 용도 | 키 패턴 | 자료구조 | TTL | 설명 |
|------|---------|---------|-----|------|
| 카운트 캐시 | `like_count:{postId}` | String | 1시간 | 좋아요/댓글/팔로우 수 |
| 피드 캐시 | `feed:{userId}:{tab}:{page}` | String (JSON) | 30초 | 피드 게시글 ID 목록 |
| 유저 캐시 | `user:{userId}` | String (JSON) | 5분 | 인증 필터에서 사용 |
| 분산 락 | `lock:{userId}:{action}:{targetId}` | String | 2초 | SETNX 기반 동시성 제어 |
| 리프레시 토큰 | `refresh_token:{token}` | String | 7일 | 토큰 → userId 매핑 |
| 유저별 토큰 | `refresh_user:{userId}` | Set | 7일 | 전체 로그아웃용 |
| 온라인 상태 | `online:{userId}` | String | 60초 | 매 요청마다 갱신 |
| 인기 검색어 | `search_popular:{date}` | Sorted Set | 7일 | 일별 검색어 랭킹 |
| 알림 읽음 | `notification_last_read:{userId}` | String | 90일 | 미읽음 판별 기준 |

<br/>

---

## 🛠️ 기술 스택

### Backend

| 구분 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.3.8, Spring Security, Spring Cloud Stream |
| ORM | Spring Data JPA, QueryDSL 5.1 |
| Database | MySQL(메인), MongoDB(알림), Redis(캐시/세션) |
| Messaging | Apache Kafka |
| Auth | JWT (Access + Refresh)|
| Build | Gradle (Kotlin DSL), 멀티 모듈 |
| Infra | Docker Compose |
| API Docs | Springdoc OpenAPI (Swagger) |

### Frontend

| 구분 | 기술 |
|------|------|
| Template | Thymeleaf (서버 사이드 라우팅) |
| Script | Vanilla JavaScript (ES5 호환) |
| Style | Custom CSS (CSS Variables, Light/Dark 테마) |

<br/>

---

## 📊 API 엔드포인트

### 인증

| Method | Endpoint | 설명 |
|--------|----------|------|
| `POST` | `/api/auth/signup` | 회원가입 |
| `POST` | `/api/auth/login` | 로그인 (JWT 발급) |
| `POST` | `/api/auth/refresh` | 토큰 갱신 |
| `POST` | `/api/auth/logout` | 로그아웃 |
| `GET` | `/api/auth/me` | 내 정보 조회 |
| `GET` | `/api/auth/verify-email` | 이메일 인증 |
| `PUT` | `/api/auth/password` | 비밀번호 변경 |
| `POST` | `/api/auth/forgot-password` | 비밀번호 재설정 요청 |
| `POST` | `/api/auth/reset-password` | 비밀번호 재설정 |

### 게시글

| Method | Endpoint | 설명 |
|--------|----------|------|
| `GET` | `/api/posts` | 피드 조회 (all / following / popular) |
| `GET` | `/api/posts/{postId}` | 게시글 상세 |
| `POST` | `/api/posts` | 게시글 생성 (JSON / Multipart) |
| `PUT` | `/api/posts/{postId}` | 게시글 수정 |
| `DELETE` | `/api/posts/{postId}` | 게시글 삭제 |
| `GET` | `/api/posts/{postId}/comments` | 댓글 목록 (대댓글 포함) |
| `GET` | `/api/posts/stats` | 게시글 통계 배치 조회 |

### 소셜

| Method | Endpoint | 설명 |
|--------|----------|------|
| `POST` | `/api/likes/{postId}` | 좋아요 토글 |
| `POST` | `/api/follows/{userId}` | 팔로우 토글 |
| `POST` | `/api/comments` | 댓글 작성 |
| `DELETE` | `/api/comments/{id}` | 댓글 삭제 |
| `POST` | `/api/bookmarks/{postId}` | 북마크 토글 |
| `GET` | `/api/bookmarks` | 북마크 목록 |
| `POST` | `/api/bookmarks/collections` | 컬렉션 생성 |

### 검색

| Method | Endpoint | 설명 |
|--------|----------|------|
| `GET` | `/api/search?q=&type=` | 통합 검색 (posts / users / tags) |
| `GET` | `/api/search/trending` | 해시태그 |
| `GET` | `/api/search/popular` | 인기 검색어 |

### 알림

| Method | Endpoint | 설명 |
|--------|----------|------|
| `GET` | `/v1/user-notifications/{userId}` | 알림 목록 (Pivot 페이징) |
| `GET` | `/v1/user-notifications/{userId}/new` | 새 알림 확인 |
| `POST` | `/v1/user-notifications/{userId}/last-read` | 읽음 처리 |

<br/>

---

## 🏗️ 설계 포인트

### 1. MSA-Ready 엔티티 설계

```java
// ❌ JPA 연관관계 사용 안 함 (모듈 분리 시 문제)
@ManyToOne
private UserEntity user;

// ✅ ID 참조만 사용 → 추후 서비스 분리 시 수정 불필요
@Column(nullable = false)
private Long userId;
```

모든 Entity에서 `@ManyToOne`, `@OneToMany`를 배제하고 **ID 참조만 사용**합니다. 현재는 같은 DB를 공유하지만, 서비스 분리 시 WebClient/Feign으로 교체하기 위해 Client 패턴을 미리 적용했습니다.

### 2. 분산 락 + Fail-Open 전략

```java
public boolean toggleLike(Long postId, Long userId) {
    if (!redisDistributedLockRepository.tryLock(userId, "like", postId)) {
        throw new IllegalStateException("Too many requests");
    }
    try {
        return doToggleLike(postId, userId);
    } finally {
        redisDistributedLockRepository.unlock(userId, "like", postId);
    }
}
```

```java
// Redis 장애 시에도 서비스 중단 없이 DB unique constraint에 위임
public boolean tryLock(...) {
    try {
        return Boolean.TRUE.equals(
            redisTemplate.opsForValue().setIfAbsent(key, "1", 2, TimeUnit.SECONDS)
        );
    } catch (Exception e) {
        return true;  // fail-open
    }
}
```

### 3. LikeNotification 집계 패턴

```java
// "A님 외 3명이 좋아합니다" 구현
public class LikeNotification extends Notification {
    private final Long postId;
    private final List<Long> likerIds;

    public void addLiker(Long likerId, ...) {
        this.likerIds.add(likerId);
    }

    public void removeLiker(Long userId, ...) {
        this.likerIds.remove(userId);
    }
}
```

같은 게시글에 대한 좋아요 알림을 개별 생성하지 않고, **하나의 MongoDB Document에 likerIds 리스트로 집계**합니다.

### 4. 이중 토큰 저장소

```
저장:    Redis (빠른 조회) + MySQL (영속성)
조회:    Redis 우선 → 캐시 미스 시 MySQL 폴백
삭제:    양쪽 동시 삭제
재설정:  전체 세션 강제 로그아웃 (양쪽 모두 삭제)
```

### 5. QueryDSL 동적 피드 쿼리

```java
// 내 글(전체) + 팔로잉(PUBLIC, FOLLOWERS_ONLY) + 기타(PUBLIC만)
BooleanExpression condition = post.userId.eq(userId)
    .or(post.userId.in(followingIds)
        .and(post.visibility.in(PUBLIC, FOLLOWERS_ONLY)))
    .or(post.userId.ne(userId)
        .and(post.userId.notIn(followingIds))
        .and(post.visibility.eq(PUBLIC)));
```

<br/>

---

<p align="center">
  <sub>Built with Java 17 · Spring Boot 3 · Kafka · Redis · MongoDB</sub>
</p>

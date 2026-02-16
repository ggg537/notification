package com.a.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // 사용자 고유 식별자

    @Column(nullable = false, unique = true, length = 50)
    private String email;               // 이메일 (로그인 ID)

    private String password;            // 암호화된 비밀번호

    @Column(nullable = false, length = 50)
    private String name;                // 사용자 이름

    @Column(length = 500)
    private String profileImageUrl;     // 프로필 이미지 URL

    @Column(length = 200)
    private String bio;                 // 자기소개

    @Column(length = 50, unique = true)
    private String handle;              // 공개적으로 나를 구분해주는 고유 닉네임 (@handle)

    @Builder.Default
    @Column(nullable = false)
    private boolean emailVerified = false;  // 이메일 인증 여부

    private LocalDateTime deletedAt;    // 계정 삭제(소프트 삭제) 시간

    @Column(length = 20)
    private String oauthProvider;       // OAuth 제공자 (google, kakao)

    @Column(length = 255)
    private String oauthProviderId;     // OAuth 제공자 고유 ID

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;    // 가입 시간

    private LocalDateTime updatedAt;    // 마지막 수정 시간

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

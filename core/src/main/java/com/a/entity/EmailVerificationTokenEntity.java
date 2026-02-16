package com.a.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification_tokens")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // 토큰 고유 식별자

    @Column(nullable = false)
    private Long userId;                // 인증 대상 사용자 ID

    @Column(nullable = false, unique = true)
    private String token;               // 이메일 인증 토큰 값

    @Column(nullable = false)
    private LocalDateTime expiresAt;    // 토큰 만료 시간

    @Builder.Default
    private boolean used = false;       // 사용 여부

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;    // 토큰 발급 시간

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

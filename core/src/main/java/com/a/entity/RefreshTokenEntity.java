package com.a.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // 토큰 고유 식별자

    @Column(nullable = false)
    private Long userId;                // 토큰 소유자 ID

    @Column(nullable = false, unique = true, length = 255)
    private String token;               // 리프레시 토큰 값

    @Column(nullable = false)
    private LocalDateTime expiresAt;    // 토큰 만료 시간

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;    // 토큰 발급 시간

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

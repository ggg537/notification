package com.a.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "likes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"postId", "userId"})
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // 좋아요 고유 식별자

    @Column(nullable = false)
    private Long postId;                // 좋아요한 게시글 ID

    @Column(nullable = false)
    private Long userId;                // 좋아요한 사용자 ID

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;    // 좋아요 시간

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

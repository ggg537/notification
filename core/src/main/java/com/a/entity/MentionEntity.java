package com.a.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // 멘션 고유 식별자

    @Column(nullable = false)
    private Long postId;                // 멘션이 포함된 게시글 ID

    @Column(nullable = false)
    private Long mentionedUserId;       // 멘션된 사용자 ID

    @Column(nullable = false)
    private Long mentionerUserId;       // 멘션한 사용자 ID

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;    // 멘션 생성 시간

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

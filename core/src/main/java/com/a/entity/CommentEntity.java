package com.a.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // 댓글 고유 식별자

    @Column(nullable = false)
    private Long postId;                // 댓글이 달린 게시글 ID

    @Column(nullable = false)
    private Long userId;                // 댓글 작성자 ID

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;             // 댓글 내용

    private Long parentId;              // 부모 댓글 ID (대댓글인 경우)

    @Builder.Default
    @Column(nullable = false)
    private int depth = 0;              // 댓글 깊이 (0: 원댓글, 1: 대댓글)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;    // 작성 시간

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

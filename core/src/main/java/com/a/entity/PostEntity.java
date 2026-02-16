package com.a.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // 게시글 고유 식별자

    @Column(nullable = false)
    private Long userId;                // 작성자 ID

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;             // 게시글 본문

    @Column(length = 500)
    private String imageUrl;            // 첨부 이미지 URL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PostVisibility visibility = PostVisibility.PUBLIC;  // 공개범위 (PUBLIC, FOLLOWERS_ONLY, PRIVATE)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;    // 작성 시간

    @Column(nullable = false)
    private LocalDateTime updatedAt;    // 마지막 수정 시간

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.visibility == null) {
            this.visibility = PostVisibility.PUBLIC;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

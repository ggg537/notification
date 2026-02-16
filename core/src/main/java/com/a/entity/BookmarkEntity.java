package com.a.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookmarks",
    uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "postId"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookmarkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // 북마크 고유 식별자

    @Column(nullable = false)
    private Long userId;                // 북마크한 사용자 ID

    @Column(nullable = false)
    private Long postId;                // 북마크된 게시글 ID

    private Long collectionId;          // 소속 컬렉션 ID (null이면 기본 저장)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;    // 북마크 시간

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

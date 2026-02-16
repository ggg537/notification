package com.a.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookmark_collections")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookmarkCollectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // 컬렉션 고유 식별자

    @Column(nullable = false)
    private Long userId;                // 컬렉션 소유자 ID

    @Column(nullable = false, length = 100)
    private String name;                // 컬렉션 이름

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;    // 생성 시간

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

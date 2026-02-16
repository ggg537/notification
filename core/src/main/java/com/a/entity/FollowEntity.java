package com.a.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "follows", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"followerId", "followingId"})
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // 팔로우 고유 식별자

    @Column(nullable = false)
    private Long followerId;            // 팔로우를 신청한 사용자 ID

    @Column(nullable = false)
    private Long followingId;           // 팔로우 대상 사용자 ID

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;    // 팔로우 시간

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

package com.a.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_hashtags",
    uniqueConstraints = @UniqueConstraint(columnNames = {"postId", "hashtagId"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostHashtagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private Long hashtagId;
}

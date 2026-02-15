package com.a.repository;

import com.a.entity.PostEntity;
import com.a.entity.PostVisibility;
import com.a.entity.QPostEntity;
import com.a.entity.QLikeEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PostEntity> findFeedPosts(Long userId, List<Long> followingIds, Pageable pageable) {
        QPostEntity post = QPostEntity.postEntity;

        // 내 게시글: 전부 | 팔로잉: PUBLIC + FOLLOWERS_ONLY | 기타: PUBLIC만
        BooleanExpression condition = post.userId.eq(userId)
            .or(post.userId.in(followingIds)
                .and(post.visibility.in(PostVisibility.PUBLIC, PostVisibility.FOLLOWERS_ONLY)))
            .or(post.userId.ne(userId)
                .and(post.userId.notIn(followingIds.isEmpty() ? List.of(0L) : followingIds))
                .and(post.visibility.eq(PostVisibility.PUBLIC)));

        List<PostEntity> content = queryFactory
            .selectFrom(post)
            .where(condition)
            .orderBy(post.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = queryFactory.select(post.count()).from(post).where(condition).fetchOne();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<PostEntity> findFollowingFeedPosts(Long userId, List<Long> followingIds, Pageable pageable) {
        QPostEntity post = QPostEntity.postEntity;

        if (followingIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        BooleanExpression condition = post.userId.in(followingIds)
            .and(post.visibility.in(PostVisibility.PUBLIC, PostVisibility.FOLLOWERS_ONLY));

        List<PostEntity> content = queryFactory
            .selectFrom(post)
            .where(condition)
            .orderBy(post.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = queryFactory.select(post.count()).from(post).where(condition).fetchOne();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<PostEntity> findPopularFeedPosts(Pageable pageable) {
        QPostEntity post = QPostEntity.postEntity;
        QLikeEntity like = QLikeEntity.likeEntity;

        // PUBLIC 게시글을 좋아요 수 기준으로 정렬
        List<PostEntity> content = queryFactory
            .selectFrom(post)
            .leftJoin(like).on(like.postId.eq(post.id))
            .where(post.visibility.eq(PostVisibility.PUBLIC))
            .groupBy(post.id)
            .orderBy(like.count().desc(), post.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = queryFactory
            .select(post.count())
            .from(post)
            .where(post.visibility.eq(PostVisibility.PUBLIC))
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<PostEntity> findUserPostsWithVisibility(Long profileUserId, Long viewerUserId, boolean isFollowing, Pageable pageable) {
        QPostEntity post = QPostEntity.postEntity;

        BooleanExpression condition = post.userId.eq(profileUserId);

        if (profileUserId.equals(viewerUserId)) {
            // 내 프로필: 전부
        } else if (isFollowing) {
            // 팔로잉: PUBLIC + FOLLOWERS_ONLY
            condition = condition.and(post.visibility.in(PostVisibility.PUBLIC, PostVisibility.FOLLOWERS_ONLY));
        } else {
            // 기타: PUBLIC만
            condition = condition.and(post.visibility.eq(PostVisibility.PUBLIC));
        }

        List<PostEntity> content = queryFactory
            .selectFrom(post)
            .where(condition)
            .orderBy(post.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = queryFactory.select(post.count()).from(post).where(condition).fetchOne();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<PostEntity> searchPosts(String keyword, Pageable pageable) {
        QPostEntity post = QPostEntity.postEntity;

        BooleanExpression condition = post.content.containsIgnoreCase(keyword)
            .and(post.visibility.eq(PostVisibility.PUBLIC));

        List<PostEntity> content = queryFactory
            .selectFrom(post)
            .where(condition)
            .orderBy(post.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = queryFactory.select(post.count()).from(post).where(condition).fetchOne();
        return new PageImpl<>(content, pageable, total);
    }
}

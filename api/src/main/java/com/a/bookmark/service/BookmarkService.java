package com.a.bookmark.service;

import com.a.entity.BookmarkCollectionEntity;
import com.a.entity.BookmarkEntity;
import com.a.repository.BookmarkCollectionRepository;
import com.a.repository.BookmarkRepository;
import com.a.repository.RedisDistributedLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final BookmarkCollectionRepository collectionRepository;
    private final RedisDistributedLockRepository redisDistributedLockRepository;

    @Transactional
    public boolean toggleBookmark(Long userId, Long postId) {
        if (!redisDistributedLockRepository.tryLock(userId, "bookmark", postId)) {
            throw new IllegalStateException("Too many requests, please try again");
        }
        try {
            Optional<BookmarkEntity> existing = bookmarkRepository.findByUserIdAndPostId(userId, postId);
            if (existing.isPresent()) {
                bookmarkRepository.delete(existing.get());
                return false;
            } else {
                BookmarkEntity bookmark = BookmarkEntity.builder()
                    .userId(userId)
                    .postId(postId)
                    .build();
                bookmarkRepository.save(bookmark);
                return true;
            }
        } finally {
            redisDistributedLockRepository.unlock(userId, "bookmark", postId);
        }
    }

    public boolean isBookmarked(Long userId, Long postId) {
        return bookmarkRepository.existsByUserIdAndPostId(userId, postId);
    }

    public List<BookmarkEntity> getBookmarks(Long userId) {
        return bookmarkRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<BookmarkEntity> getBookmarksByCollection(Long collectionId) {
        return bookmarkRepository.findAllByCollectionIdOrderByCreatedAtDesc(collectionId);
    }

    public List<BookmarkCollectionEntity> getCollections(Long userId) {
        return collectionRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public BookmarkCollectionEntity createCollection(Long userId, String name) {
        BookmarkCollectionEntity collection = BookmarkCollectionEntity.builder()
            .userId(userId)
            .name(name)
            .build();
        return collectionRepository.save(collection);
    }

    @Transactional
    public BookmarkCollectionEntity updateCollection(Long collectionId, Long userId, String name) {
        BookmarkCollectionEntity collection = collectionRepository.findById(collectionId)
            .orElseThrow(() -> new IllegalArgumentException("Collection not found"));
        if (!collection.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Not authorized");
        }
        collection.setName(name);
        return collectionRepository.save(collection);
    }

    @Transactional
    public void deleteCollection(Long collectionId, Long userId) {
        BookmarkCollectionEntity collection = collectionRepository.findById(collectionId)
            .orElseThrow(() -> new IllegalArgumentException("Collection not found"));
        if (!collection.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Not authorized");
        }
        // 이 컬렉션의 북마크를 미분류(null)로 이동
        List<BookmarkEntity> bookmarks = bookmarkRepository.findAllByCollectionIdOrderByCreatedAtDesc(collectionId);
        bookmarks.forEach(b -> b.setCollectionId(null));
        bookmarkRepository.saveAll(bookmarks);
        collectionRepository.delete(collection);
    }

    @Transactional
    public void moveBookmarkToCollection(Long userId, Long postId, Long collectionId) {
        BookmarkEntity bookmark = bookmarkRepository.findByUserIdAndPostId(userId, postId)
            .orElseThrow(() -> new IllegalArgumentException("Bookmark not found"));
        if (collectionId != null) {
            BookmarkCollectionEntity collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));
            if (!collection.getUserId().equals(userId)) {
                throw new IllegalArgumentException("Not authorized");
            }
        }
        bookmark.setCollectionId(collectionId);
        bookmarkRepository.save(bookmark);
    }
}

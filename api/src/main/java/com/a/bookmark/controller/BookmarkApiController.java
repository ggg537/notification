package com.a.bookmark.controller;

import com.a.bookmark.dto.*;
import com.a.bookmark.service.BookmarkService;
import com.a.common.dto.MessageResponse;
import com.a.entity.BookmarkCollectionEntity;
import com.a.entity.BookmarkEntity;
import com.a.entity.PostEntity;
import com.a.post.dto.PostResponse;
import com.a.post.service.PostEnrichmentService;
import com.a.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkApiController {

    private final BookmarkService bookmarkService;
    private final PostService postService;
    private final PostEnrichmentService postEnrichmentService;

    @PostMapping("/{postId}")
    public ResponseEntity<BookmarkToggleResponse> toggleBookmark(
        @PathVariable Long postId,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        boolean bookmarked = bookmarkService.toggleBookmark(userId, postId);
        return ResponseEntity.ok(new BookmarkToggleResponse(bookmarked));
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getBookmarks(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<BookmarkEntity> bookmarks = bookmarkService.getBookmarks(userId);
        List<PostEntity> posts = bookmarks.stream()
            .map(b -> postService.getPost(b.getPostId()))
            .toList();
        return ResponseEntity.ok(postEnrichmentService.enrichPosts(posts, userId));
    }

    @GetMapping("/collections")
    public ResponseEntity<List<BookmarkCollectionResponse>> getCollections(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<BookmarkCollectionResponse> collections = bookmarkService.getCollections(userId).stream()
            .map(BookmarkCollectionResponse::from)
            .toList();
        return ResponseEntity.ok(collections);
    }

    @PostMapping("/collections")
    public ResponseEntity<BookmarkCollectionResponse> createCollection(
        @RequestBody BookmarkCollectionRequest request,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        BookmarkCollectionEntity collection = bookmarkService.createCollection(userId, request.name());
        return ResponseEntity.ok(BookmarkCollectionResponse.from(collection));
    }

    @PutMapping("/collections/{id}")
    public ResponseEntity<BookmarkCollectionResponse> updateCollection(
        @PathVariable Long id,
        @RequestBody BookmarkCollectionRequest request,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        BookmarkCollectionEntity collection = bookmarkService.updateCollection(id, userId, request.name());
        return ResponseEntity.ok(BookmarkCollectionResponse.from(collection));
    }

    @DeleteMapping("/collections/{id}")
    public ResponseEntity<MessageResponse> deleteCollection(
        @PathVariable Long id,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        bookmarkService.deleteCollection(id, userId);
        return ResponseEntity.ok(new MessageResponse("Deleted"));
    }

    @PutMapping("/{postId}/collection/{collectionId}")
    public ResponseEntity<MessageResponse> moveToCollection(
        @PathVariable Long postId,
        @PathVariable Long collectionId,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        bookmarkService.moveBookmarkToCollection(userId, postId, collectionId);
        return ResponseEntity.ok(new MessageResponse("Moved"));
    }

    @GetMapping("/collections/{id}/posts")
    public ResponseEntity<List<PostResponse>> getCollectionPosts(
        @PathVariable Long id,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        List<BookmarkEntity> bookmarks = bookmarkService.getBookmarksByCollection(id);
        List<PostEntity> posts = bookmarks.stream()
            .map(b -> postService.getPost(b.getPostId()))
            .toList();
        return ResponseEntity.ok(postEnrichmentService.enrichPosts(posts, userId));
    }
}

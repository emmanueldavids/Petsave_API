package com.petsave.petsave.Controller;

import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Service.PostService;
import com.petsave.petsave.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class PostController {

    private final PostService postService;

    // Post CRUD operations
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody PostRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("POST /api/posts - Creating post for user: {}", currentUser.getEmail());
        
        try {
            PostResponse response = postService.createPost(request, currentUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating post: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/posts - Fetching posts page {} size {}", page, size);
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<PostResponse> posts = postService.getPosts(pageable, currentUser);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            log.error("Error fetching posts: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/posts/{} - Fetching post", postId);
        
        try {
            PostResponse response = postService.getPost(postId, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error fetching post: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching post: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("PUT /api/posts/{} - Updating post", postId);
        
        try {
            PostResponse response = postService.updatePost(postId, request, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error updating post: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating post: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal User currentUser) {
        log.info("DELETE /api/posts/{} - Deleting post", postId);
        
        try {
            postService.deletePost(postId, currentUser);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Error deleting post: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error deleting post: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Search operations
    @GetMapping("/search")
    public ResponseEntity<Page<PostResponse>> searchPosts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/posts/search - Searching posts with query: {}", query);
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<PostResponse> posts = postService.searchPosts(query, pageable, currentUser);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            log.error("Error searching posts: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // User posts
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostResponse>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/posts/user/{} - Fetching posts for user", userId);
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<PostResponse> posts = postService.getUserPosts(userId, pageable, currentUser);
            return ResponseEntity.ok(posts);
        } catch (RuntimeException e) {
            log.error("Error fetching user posts: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error fetching user posts: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Like operations
    @PostMapping("/{postId}/like")
    public ResponseEntity<PostResponse> likePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal User currentUser) {
        log.info("POST /api/posts/{}/like - Liking post", postId);
        
        try {
            PostResponse response = postService.likePost(postId, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error liking post: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error liking post: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<PostResponse> unlikePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal User currentUser) {
        log.info("DELETE /api/posts/{}/like - Unliking post", postId);
        
        try {
            PostResponse response = postService.unlikePost(postId, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error unliking post: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error unliking post: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Comment operations
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("POST /api/posts/{}/comments - Creating comment", postId);
        
        try {
            CommentResponse response = postService.createComment(postId, request, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error creating comment: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating comment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<Page<CommentResponse>> getPostComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/posts/{}/comments - Fetching comments", postId);
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<CommentResponse> comments = postService.getPostComments(postId, pageable, currentUser);
            return ResponseEntity.ok(comments);
        } catch (RuntimeException e) {
            log.error("Error fetching comments: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error fetching comments: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Comment like operations
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<CommentResponse> likeComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal User currentUser) {
        log.info("POST /api/posts/comments/{}/like - Liking comment", commentId);
        
        try {
            CommentResponse response = postService.likeComment(commentId, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error liking comment: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error liking comment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/comments/{commentId}/like")
    public ResponseEntity<CommentResponse> unlikeComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal User currentUser) {
        log.info("DELETE /api/posts/comments/{}/like - Unliking comment", commentId);
        
        try {
            CommentResponse response = postService.unlikeComment(commentId, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error unliking comment: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error unliking comment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}

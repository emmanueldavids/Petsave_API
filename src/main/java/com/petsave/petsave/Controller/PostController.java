package com.petsave.petsave.Controller;

import com.petsave.petsave.Service.PostService;
import com.petsave.petsave.dto.CommentRequest;
import com.petsave.petsave.dto.CommentResponse;
import com.petsave.petsave.dto.PostRequest;
import com.petsave.petsave.dto.PostResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<Page<PostResponse>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String tag) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(postService.getFeed(tag, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest request) {
        return ResponseEntity.ok(postService.createPost(request));
    }

    @PostMapping(value = "/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> createPostWithImages(
            @RequestParam("content") String content,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        return ResponseEntity.ok(postService.createPostWithImages(content, location, tags, images));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long id, @Valid @RequestBody PostRequest request) {
        return ResponseEntity.ok(postService.updatePost(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<PostResponse> likePost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.likePost(id));
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<PostResponse> unlikePost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.unlikePost(id));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long id, @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(postService.addComment(id, request));
    }

    @PatchMapping("/{id}/pin")
    public ResponseEntity<PostResponse> togglePin(@PathVariable Long id) {
        return ResponseEntity.ok(postService.togglePin(id));
    }

    @PatchMapping("/{id}/hide")
    public ResponseEntity<PostResponse> toggleHide(@PathVariable Long id) {
        return ResponseEntity.ok(postService.toggleHide(id));
    }
}

package com.petsave.petsave.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.petsave.petsave.Service.BlogService;
import com.petsave.petsave.dto.BlogRequest;
import com.petsave.petsave.dto.BlogResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @GetMapping
    public List<BlogResponse> getAllBlogs() {
        return blogService.getAllBlogs();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BlogResponse> createBlog(@ModelAttribute BlogRequest blogRequest) {
        BlogResponse created = blogService.createBlog(blogRequest);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogResponse> getBlogById(@PathVariable Long id) {
        BlogResponse blog = blogService.getBlogById(id);
        return blog != null ? ResponseEntity.ok(blog) : ResponseEntity.notFound().build();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BlogResponse> updateBlog(@PathVariable Long id, @ModelAttribute BlogRequest blogRequest) {
        BlogResponse updated = blogService.updateBlog(id, blogRequest);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBlog(@PathVariable Long id) {
        try {
            boolean deleted = blogService.deleteBlog(id);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Blog deleted successfully"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
        return ResponseEntity.status(404).body(Map.of("error", "Blog not found"));
    }
}

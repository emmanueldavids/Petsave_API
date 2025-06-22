package com.petsave.petsave.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.petsave.petsave.Entity.Blog;
import com.petsave.petsave.Repository.BlogRepository;
import com.petsave.petsave.Service.BlogService;
import com.petsave.petsave.dto.BlogRequest;
import com.petsave.petsave.dto.BlogResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogController {
    // This controller will handle blog-related endpoints
    // You can add methods to create, read, update, and delete blogs
    // For example:
    private final BlogService blogService;
    private final BlogRepository blogRepository;

    @GetMapping
    public List<BlogResponse> getAllBlogs() {
        return blogService.getAllBlogs();
    }
    
    @PostMapping
    public BlogResponse createBlog(@RequestBody BlogRequest blogRequest) {
        return blogService.createBlog(blogRequest);
    }
    
    @GetMapping("/{id}")
    public BlogResponse getBlogById(@PathVariable Long id) {
        return blogService.getBlogById(id);
    }
    
    @PutMapping("/{id}")
    public BlogResponse updateBlog(@PathVariable Long id, @RequestBody BlogRequest blogRequest) {
        return blogService.updateBlog(id, blogRequest);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBlog(@PathVariable Long id) throws Exception {
        boolean deleted = blogService.deleteBlog(id);
        if (deleted) {
            return ResponseEntity.ok().body(Map.of("message", "Blog deleted successfully"));
        } else {
            return ResponseEntity.status(404).body(Map.of("error", "Blog not found"));
        }
    }

    
}
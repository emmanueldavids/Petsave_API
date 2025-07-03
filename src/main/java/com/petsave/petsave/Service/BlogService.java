package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.Blog;
import com.petsave.petsave.Repository.BlogRepository;
import com.petsave.petsave.dto.BlogRequest;
import com.petsave.petsave.dto.BlogResponse;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlogService {

    private final BlogRepository blogRepository;
    private final String uploadDir = "uploads/blog-images/";

    public BlogService(BlogRepository blogRepository) {
        this.blogRepository = blogRepository;
    }

    public List<BlogResponse> getAllBlogs() {
        return blogRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public BlogResponse createBlog(BlogRequest blogRequest) {
        Blog blog = new Blog();
        blog.setTitle(blogRequest.getTitle());
        blog.setContent(blogRequest.getContent());
        blog.setAuthor(blogRequest.getAuthor());

        MultipartFile image = blogRequest.getImage();
        if (image != null && !image.isEmpty()) {
            String imagePath = saveImage(image);
            blog.setImageUrl(imagePath);
        }

        blog = blogRepository.save(blog);
        return mapToResponse(blog);
    }

    public BlogResponse getBlogById(Long id) {
        return blogRepository.findById(id)
            .map(this::mapToResponse)
            .orElse(null);
    }

    public BlogResponse updateBlog(Long id, BlogRequest blogRequest) {
        return blogRepository.findById(id).map(existing -> {
            existing.setTitle(blogRequest.getTitle());
            existing.setContent(blogRequest.getContent());
            existing.setAuthor(blogRequest.getAuthor());

            MultipartFile image = blogRequest.getImage();
            if (image != null && !image.isEmpty()) {
                String imagePath = saveImage(image);
                existing.setImageUrl(imagePath);
            }

            Blog updatedBlog = blogRepository.save(existing);
            return mapToResponse(updatedBlog);
        }).orElse(null);
    }

    public boolean deleteBlog(Long id) throws Exception {
        if (blogRepository.existsById(id)) {
            blogRepository.deleteById(id);
            return true;
        } else {
            throw new Exception("Blog not found with id: " + id);
        }
    }

    private String saveImage(MultipartFile image) {
        try {
            Files.createDirectories(Paths.get(uploadDir));
            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            Files.write(filePath, image.getBytes());
            return "/uploads/blog-images/" + fileName; // returned as public URL path
        } catch (Exception e) {
            throw new RuntimeException("Failed to save image", e);
        }
    }

    private BlogResponse mapToResponse(Blog blog) {
        BlogResponse response = new BlogResponse();
        response.setId(blog.getId());
        response.setTitle(blog.getTitle());
        response.setContent(blog.getContent());
        response.setAuthor(blog.getAuthor());
        response.setImageUrl(blog.getImageUrl());
        return response;
    }
}

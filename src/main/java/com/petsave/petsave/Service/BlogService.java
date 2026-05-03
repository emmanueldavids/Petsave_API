package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.Blog;
import com.petsave.petsave.Repository.BlogRepository;
import com.petsave.petsave.dto.BlogRequest;
import com.petsave.petsave.dto.BlogResponse;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BlogService {

    private final BlogRepository blogRepository;
    private final Cloudinary cloudinary;

    public BlogService(BlogRepository blogRepository, Cloudinary cloudinary) {
        this.blogRepository = blogRepository;
        this.cloudinary = cloudinary;
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
        blog.setCreatedAt(java.time.LocalDateTime.now());

        MultipartFile image = blogRequest.getImage();
        if (image != null && !image.isEmpty()) {
            try {
                // Upload image to Cloudinary
                Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    image.getBytes(),
                    ObjectUtils.asMap(
                        "folder", "blog-images",
                        "public_id", "blog_" + System.currentTimeMillis(),
                        "resource_type", "auto"
                    )
                );
                
                // Set Cloudinary URL
                String imageUrl = (String) uploadResult.get("secure_url");
                blog.setImageUrl(imageUrl);
                blog.setImageType(image.getContentType());
                
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
            }
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
                try {
                    // Upload new image to Cloudinary
                    Map<String, Object> uploadResult = cloudinary.uploader().upload(
                        image.getBytes(),
                        ObjectUtils.asMap(
                            "folder", "blog-images",
                            "public_id", "blog_" + System.currentTimeMillis(),
                            "resource_type", "auto"
                        )
                    );
                    
                    // Set new Cloudinary URL
                    String imageUrl = (String) uploadResult.get("secure_url");
                    existing.setImageUrl(imageUrl);
                    existing.setImageType(image.getContentType());
                    
                } catch (IOException e) {
                    throw new RuntimeException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
                }
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

    private BlogResponse mapToResponse(Blog blog) {
        BlogResponse response = new BlogResponse();
        response.setId(blog.getId());
        response.setTitle(blog.getTitle());
        response.setContent(blog.getContent());
        response.setAuthor(blog.getAuthor());
        response.setImageUrl(blog.getImageUrl());
        response.setCreatedAt(blog.getCreatedAt());
        return response;
    }
}

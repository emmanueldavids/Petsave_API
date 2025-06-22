package com.petsave.petsave.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.petsave.petsave.Entity.Blog;
import com.petsave.petsave.Repository.BlogRepository;
import com.petsave.petsave.dto.BlogRequest;
import com.petsave.petsave.dto.BlogResponse;

@Service
public class BlogService {

    private final BlogRepository blogRepository;

    public BlogService(BlogRepository blogRepository) {
        this.blogRepository = blogRepository;
    }

    // Implement service methods here   
    public List<BlogResponse> getAllBlogs() {
        return blogRepository.findAll().stream()
            .map(blog -> {
                BlogResponse response = new BlogResponse();
                response.setId(blog.getId());
                response.setTitle(blog.getTitle());
                response.setContent(blog.getContent());
                response.setAuthor(blog.getAuthor());
                return response;
            })
            .collect(Collectors.toList());
    }

    public BlogResponse createBlog(BlogRequest blogRequest) {
        Blog blog = new Blog();
        blog.setTitle(blogRequest.getTitle());
        blog.setContent(blogRequest.getContent());
        blog.setAuthor(blogRequest.getAuthor());
        blog = blogRepository.save(blog);
        BlogResponse response = new BlogResponse();
        response.setId(blog.getId());
        response.setTitle(blog.getTitle());
        response.setContent(blog.getContent());
        response.setAuthor(blog.getAuthor());
        return response;
    }

    public BlogResponse getBlogById(Long id) {
        return blogRepository.findById(id).map(blog -> {
            BlogResponse response = new BlogResponse();
            response.setId(blog.getId());
            response.setTitle(blog.getTitle());
            response.setContent(blog.getContent());
            response.setAuthor(blog.getAuthor());
            return response;
        }).orElse(null);
    }

    public BlogResponse updateBlog(Long id, BlogRequest blogRequest) {
        if (blogRepository.existsById(id)) {
            Blog blog = new Blog();
            blog.setId(id);
            blog.setTitle(blogRequest.getTitle());
            blog.setContent(blogRequest.getContent());
            blog.setAuthor(blogRequest.getAuthor());
            blog = blogRepository.save(blog);
            BlogResponse response = new BlogResponse();
            response.setId(blog.getId());
            response.setTitle(blog.getTitle());
            response.setContent(blog.getContent());
            response.setAuthor(blog.getAuthor());
            return response;
        }
        return null; // or throw an exception
    }
    public boolean deleteBlog(Long id) throws Exception {
        if (blogRepository.existsById(id)) {
            blogRepository.deleteById(id);
            return true;
        } else {
            throw new Exception("Blog not found with id: " + id);
        }
    }
    // Add more methods as needed for your application
}
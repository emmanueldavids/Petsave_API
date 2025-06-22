package com.petsave.petsave.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.petsave.petsave.Entity.Blog;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    // Custom query methods can be added here if needed
    // For example, to find blogs by title or author
    List<Blog> findByTitleContainingIgnoreCase(String title);
    List<Blog> findByAuthorContainingIgnoreCase(String author);
    
}

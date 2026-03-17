package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.Post;
import com.petsave.petsave.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    List<Post> findByAuthorOrderByCreatedAtDesc(User author);
    
    Page<Post> findByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);
    
    Page<Post> findByAuthorAndIsDeletedFalseOrderByCreatedAtDesc(User author, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.isDeleted = false AND " +
           "(LOWER(p.content) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.author.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.location) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Post> searchPosts(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT p FROM Post p JOIN p.tags t WHERE t IN :tags AND p.isDeleted = false")
    Page<Post> findByTags(@Param("tags") List<String> tags, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.isDeleted = false ORDER BY p.likesCount DESC")
    Page<Post> findPopularPosts(Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Post p WHERE p.author = :author AND p.isDeleted = false")
    Long countByAuthor(@Param("author") User author);
    
    @Query("SELECT p FROM Post p WHERE p.author.id IN :userIds AND p.isDeleted = false ORDER BY p.createdAt DESC")
    Page<Post> findByAuthorIds(@Param("userIds") List<Long> userIds, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.isDeleted = false AND p.createdAt >= :since ORDER BY p.createdAt DESC")
    List<Post> findRecentPosts(@Param("since") java.time.LocalDateTime since);
}

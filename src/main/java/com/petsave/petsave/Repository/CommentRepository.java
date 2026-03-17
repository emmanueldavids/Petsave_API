package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.Comment;
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
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    List<Comment> findByPostAndParentCommentIsNullOrderByCreatedAtDesc(Post post);
    
    Page<Comment> findByPostAndParentCommentIsNullOrderByCreatedAtDesc(Post post, Pageable pageable);
    
    List<Comment> findByParentCommentOrderByCreatedAtAsc(Comment parentComment);
    
    List<Comment> findByAuthorOrderByCreatedAtDesc(User author);
    
    Page<Comment> findByAuthorAndIsDeletedFalseOrderByCreatedAtDesc(User author, Pageable pageable);
    
    @Query("SELECT c FROM Comment c WHERE c.post = :post AND c.isDeleted = false ORDER BY c.createdAt DESC")
    List<Comment> findByPostAndIsDeletedFalse(@Param("post") Post post);
    
    @Query("SELECT c FROM Comment c WHERE c.parentComment = :parentComment AND c.isDeleted = false ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentCommentAndIsDeletedFalse(@Param("parentComment") Comment parentComment);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.author = :author AND c.isDeleted = false")
    Long countByAuthor(@Param("author") User author);
    
    @Query("SELECT c FROM Comment c WHERE c.post.id IN :postIds AND c.isDeleted = false ORDER BY c.createdAt DESC")
    List<Comment> findByPostIds(@Param("postIds") List<Long> postIds);
    
    @Query("SELECT c FROM Comment c WHERE c.isDeleted = false AND " +
           "(LOWER(c.content) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.author.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Comment> searchComments(@Param("search") String search, Pageable pageable);
}

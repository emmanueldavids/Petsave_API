package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.Comment;
import com.petsave.petsave.Entity.CommentLike;
import com.petsave.petsave.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    
    Optional<CommentLike> findByCommentAndUser(Comment comment, User user);
    
    List<CommentLike> findByComment(Comment comment);
    
    List<CommentLike> findByUser(User user);
    
    @Query("SELECT COUNT(cl) FROM CommentLike cl WHERE cl.comment = :comment")
    Long countByComment(@Param("comment") Comment comment);
    
    @Query("SELECT COUNT(cl) FROM CommentLike cl WHERE cl.user = :user")
    Long countByUser(@Param("user") User user);
    
    @Query("SELECT CASE WHEN COUNT(cl) > 0 THEN true ELSE false END FROM CommentLike cl WHERE cl.comment = :comment AND cl.user = :user")
    boolean existsByCommentAndUser(@Param("comment") Comment comment, @Param("user") User user);
    
    @Query("SELECT cl.user FROM CommentLike cl WHERE cl.comment = :comment")
    List<User> findUsersWhoLikedComment(@Param("comment") Comment comment);
    
    @Query("SELECT cl.comment FROM CommentLike cl WHERE cl.user = :user")
    List<Comment> findCommentsLikedByUser(@Param("user") User user);
}

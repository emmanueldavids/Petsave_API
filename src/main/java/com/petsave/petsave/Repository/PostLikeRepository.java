package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.Post;
import com.petsave.petsave.Entity.PostLike;
import com.petsave.petsave.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    
    Optional<PostLike> findByPostAndUser(Post post, User user);
    
    List<PostLike> findByPost(Post post);
    
    List<PostLike> findByUser(User user);
    
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post = :post")
    Long countByPost(@Param("post") Post post);
    
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.user = :user")
    Long countByUser(@Param("user") User user);
    
    @Query("SELECT CASE WHEN COUNT(pl) > 0 THEN true ELSE false END FROM PostLike pl WHERE pl.post = :post AND pl.user = :user")
    boolean existsByPostAndUser(@Param("post") Post post, @Param("user") User user);
    
    @Query("SELECT pl.user FROM PostLike pl WHERE pl.post = :post")
    List<User> findUsersWhoLikedPost(@Param("post") Post post);
    
    @Query("SELECT pl.post FROM PostLike pl WHERE pl.user = :user")
    List<Post> findPostsLikedByUser(@Param("user") User user);
}

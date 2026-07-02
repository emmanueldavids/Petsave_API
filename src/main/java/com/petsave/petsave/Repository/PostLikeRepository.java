package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.Post;
import com.petsave.petsave.Entity.PostLike;
import com.petsave.petsave.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostAndUser(Post post, User user);
    boolean existsByPostAndUser(Post post, User user);
}

package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Override
    @EntityGraph(attributePaths = {"author"})
    Optional<Post> findById(Long id);

    @EntityGraph(attributePaths = {"author"})
    @Query("SELECT p FROM Post p WHERE p.isHidden = false ORDER BY p.isPinned DESC, p.createdAt DESC")
    Page<Post> findFeed(Pageable pageable);

    @EntityGraph(attributePaths = {"author"})
    @Query("SELECT p FROM Post p JOIN p.tags t WHERE p.isHidden = false AND t = :tag ORDER BY p.isPinned DESC, p.createdAt DESC")
    Page<Post> findFeedByTag(@Param("tag") String tag, Pageable pageable);
}

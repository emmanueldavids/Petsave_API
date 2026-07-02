package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.Post;
import com.petsave.petsave.Entity.PostComment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    @Override
    @EntityGraph(attributePaths = {"author", "post"})
    Optional<PostComment> findById(Long id);

    @EntityGraph(attributePaths = {"author"})
    List<PostComment> findByPostOrderByCreatedAtAsc(Post post);
}

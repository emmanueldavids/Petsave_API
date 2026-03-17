package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.*;
import com.petsave.petsave.Repository.*;
import com.petsave.petsave.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserRepository userRepository;

    // Post operations
    public PostResponse createPost(PostRequest request, User author) {
        log.info("Creating post for user: {}", author.getEmail());

        Post post = Post.builder()
                .author(author)
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .location(request.getLocation())
                .tags(request.getTags() != null ? new java.util.HashSet<>(request.getTags()) : new java.util.HashSet<>())
                .build();

        post = postRepository.save(post);
        author.incrementPostsCount();
        userRepository.save(author);

        log.info("Post created successfully with ID: {}", post.getId());
        return convertToPostResponse(post, author);
    }

    public Page<PostResponse> getPosts(Pageable pageable, User currentUser) {
        log.info("Fetching posts for page: {}", pageable.getPageNumber());
        
        Page<Post> posts = postRepository.findByIsDeletedFalseOrderByCreatedAtDesc(pageable);
        return posts.map(post -> convertToPostResponse(post, currentUser));
    }

    public Page<PostResponse> getUserPosts(Long userId, Pageable pageable, User currentUser) {
        log.info("Fetching posts for user: {}", userId);
        
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Page<Post> posts = postRepository.findByAuthorAndIsDeletedFalseOrderByCreatedAtDesc(author, pageable);
        return posts.map(post -> convertToPostResponse(post, currentUser));
    }

    public Page<PostResponse> searchPosts(String search, Pageable pageable, User currentUser) {
        log.info("Searching posts with query: {}", search);
        
        Page<Post> posts = postRepository.searchPosts(search, pageable);
        return posts.map(post -> convertToPostResponse(post, currentUser));
    }

    public PostResponse getPost(Long postId, User currentUser) {
        log.info("Fetching post with ID: {}", postId);
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        if (post.getIsDeleted()) {
            throw new RuntimeException("Post has been deleted");
        }
        
        return convertToPostResponse(post, currentUser);
    }

    public PostResponse updatePost(Long postId, PostRequest request, User currentUser) {
        log.info("Updating post with ID: {}", postId);
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        if (!post.getAuthor().equals(currentUser)) {
            throw new RuntimeException("You can only update your own posts");
        }
        
        if (post.getIsDeleted()) {
            throw new RuntimeException("Cannot update deleted post");
        }
        
        post.setContent(request.getContent());
        post.setImageUrl(request.getImageUrl());
        post.setLocation(request.getLocation());
        post.setTags(request.getTags() != null ? new java.util.HashSet<>(request.getTags()) : new java.util.HashSet<>());
        
        post = postRepository.save(post);
        log.info("Post updated successfully");
        
        return convertToPostResponse(post, currentUser);
    }

    public void deletePost(Long postId, User currentUser) {
        log.info("Deleting post with ID: {}", postId);
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        if (!post.getAuthor().equals(currentUser)) {
            throw new RuntimeException("You can only delete your own posts");
        }
        
        post.setIsDeleted(true);
        postRepository.save(post);
        
        currentUser.decrementPostsCount();
        userRepository.save(currentUser);
        
        log.info("Post deleted successfully");
    }

    // Like operations
    public PostResponse likePost(Long postId, User currentUser) {
        log.info("Liking post with ID: {}", postId);
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        if (post.getIsDeleted()) {
            throw new RuntimeException("Cannot like deleted post");
        }
        
        if (post.isLikedByUser(currentUser)) {
            throw new RuntimeException("Post already liked");
        }
        
        post.addLike(currentUser);
        postRepository.save(post);
        
        log.info("Post liked successfully");
        return convertToPostResponse(post, currentUser);
    }

    public PostResponse unlikePost(Long postId, User currentUser) {
        log.info("Unliking post with ID: {}", postId);
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        if (post.getIsDeleted()) {
            throw new RuntimeException("Cannot unlike deleted post");
        }
        
        if (!post.isLikedByUser(currentUser)) {
            throw new RuntimeException("Post not liked");
        }
        
        post.removeLike(currentUser);
        postRepository.save(post);
        
        log.info("Post unliked successfully");
        return convertToPostResponse(post, currentUser);
    }

    // Comment operations
    public CommentResponse createComment(Long postId, CommentRequest request, User currentUser) {
        log.info("Creating comment on post: {}", postId);
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        if (post.getIsDeleted()) {
            throw new RuntimeException("Cannot comment on deleted post");
        }
        
        Comment comment = Comment.builder()
                .post(post)
                .author(currentUser)
                .content(request.getContent())
                .build();
        
        // Handle parent comment for replies
        if (request.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParentComment(parentComment);
        }
        
        comment = commentRepository.save(comment);
        post.addComment(comment);
        postRepository.save(post);
        
        log.info("Comment created successfully with ID: {}", comment.getId());
        return convertToCommentResponse(comment, currentUser);
    }

    public Page<CommentResponse> getPostComments(Long postId, Pageable pageable, User currentUser) {
        log.info("Fetching comments for post: {}", postId);
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        Page<Comment> comments = commentRepository.findByPostAndParentCommentIsNullOrderByCreatedAtDesc(post, pageable);
        return comments.map(comment -> convertToCommentResponse(comment, currentUser));
    }

    public CommentResponse likeComment(Long commentId, User currentUser) {
        log.info("Liking comment with ID: {}", commentId);
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        if (comment.getIsDeleted()) {
            throw new RuntimeException("Cannot like deleted comment");
        }
        
        if (comment.isLikedByUser(currentUser)) {
            throw new RuntimeException("Comment already liked");
        }
        
        comment.addLike(currentUser);
        commentRepository.save(comment);
        
        log.info("Comment liked successfully");
        return convertToCommentResponse(comment, currentUser);
    }

    public CommentResponse unlikeComment(Long commentId, User currentUser) {
        log.info("Unliking comment with ID: {}", commentId);
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        if (comment.getIsDeleted()) {
            throw new RuntimeException("Cannot unlike deleted comment");
        }
        
        if (!comment.isLikedByUser(currentUser)) {
            throw new RuntimeException("Comment not liked");
        }
        
        comment.removeLike(currentUser);
        commentRepository.save(comment);
        
        log.info("Comment unliked successfully");
        return convertToCommentResponse(comment, currentUser);
    }

    // Helper methods
    private PostResponse convertToPostResponse(Post post, User currentUser) {
        List<CommentResponse> comments = commentRepository.findByPostAndParentCommentIsNullOrderByCreatedAtDesc(post)
                .stream()
                .limit(3) // Limit to 3 recent comments for performance
                .map(comment -> convertToCommentResponse(comment, currentUser))
                .collect(Collectors.toList());

        List<UserResponse> likedUsers = postLikeRepository.findByPost(post)
                .stream()
                .limit(10) // Limit to 10 users for performance
                .map(postLike -> convertToUserResponse(postLike.getUser()))
                .collect(Collectors.toList());

        return PostResponse.builder()
                .id(post.getId())
                .author(convertToUserResponse(post.getAuthor()))
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .location(post.getLocation())
                .tags(post.getTags())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .sharesCount(post.getSharesCount())
                .isLiked(post.isLikedByUser(currentUser))
                .createdAt(post.getCreatedAt())
                .timeAgo(post.getTimeAgo())
                .comments(comments)
                .likedUsers(likedUsers)
                .build();
    }

    private CommentResponse convertToCommentResponse(Comment comment, User currentUser) {
        List<CommentResponse> replies = commentRepository.findByParentCommentOrderByCreatedAtAsc(comment)
                .stream()
                .map(reply -> convertToCommentResponse(reply, currentUser))
                .collect(Collectors.toList());

        List<UserResponse> likedUsers = commentLikeRepository.findByComment(comment)
                .stream()
                .map(commentLike -> convertToUserResponse(commentLike.getUser()))
                .collect(Collectors.toList());

        return CommentResponse.builder()
                .id(comment.getId())
                .author(convertToUserResponse(comment.getAuthor()))
                .content(comment.getContent())
                .likesCount(comment.getLikesCount())
                .isLiked(comment.isLikedByUser(currentUser))
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .createdAt(comment.getCreatedAt())
                .timeAgo(comment.getTimeAgo())
                .replies(replies)
                .likedUsers(likedUsers)
                .build();
    }

    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .bio(user.getBio())
                .location(user.getLocation())
                .avatarUrl(user.getAvatarUrl())
                .postsCount(user.getPostsCount())
                .petsAdopted(user.getPetsAdopted())
                .isOnline(user.getIsOnline())
                .lastSeen(user.getLastSeen())
                .lastSeenTimeAgo(user.getLastSeenTimeAgo())
                .joinedDate(user.getCreatedAt())
                .joinedTimeAgo(user.getCreatedAt() != null ? getTimeAgo(user.getCreatedAt()) : null)
                .build();
    }

    private String getTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "Unknown";
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(dateTime, now).toMinutes();
        
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " min ago";
        if (minutes < 1440) return (minutes / 60) + " hours ago";
        if (minutes < 10080) return (minutes / 1440) + " days ago";
        return (minutes / 10080) + " weeks ago";
    }
}

package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.Post;
import com.petsave.petsave.Entity.PostComment;
import com.petsave.petsave.Entity.PostLike;
import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Repository.PostCommentRepository;
import com.petsave.petsave.Repository.PostLikeRepository;
import com.petsave.petsave.Repository.PostRepository;
import com.petsave.petsave.Repository.UserRepository;
import com.petsave.petsave.dto.CommentRequest;
import com.petsave.petsave.dto.CommentResponse;
import com.petsave.petsave.dto.PostRequest;
import com.petsave.petsave.dto.PostResponse;
import com.petsave.petsave.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public PostResponse createPost(PostRequest request) {
        User author = getCurrentUser();
        Post post = new Post();
        post.setAuthor(author);
        post.setContent(request.getContent());
        post.setLocation(request.getLocation());
        if (request.getTags() != null) {
            post.setTags(new HashSet<>(request.getTags()));
        }
        Post saved = postRepository.save(post);
        return mapToResponse(saved, author, false);
    }

    public PostResponse createPostWithImages(String content, String location, List<String> tags, List<MultipartFile> images) {
        User author = getCurrentUser();
        Post post = new Post();
        post.setAuthor(author);
        post.setContent(content);
        post.setLocation(location);
        if (tags != null) {
            post.setTags(new HashSet<>(tags));
        }
        if (images != null) {
            List<String> uploadedUrls = new ArrayList<>();
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    uploadedUrls.add(cloudinaryService.uploadFile(image, "post-images"));
                }
            }
            post.setImageUrls(uploadedUrls);
        }
        Post saved = postRepository.save(post);
        return mapToResponse(saved, author, false);
    }

    public Page<PostResponse> getFeed(String tag, Pageable pageable) {
        User currentUser = getCurrentUserOrNull();
        Page<Post> page = (tag == null || tag.isBlank())
                ? postRepository.findFeed(pageable)
                : postRepository.findFeedByTag(tag, pageable);
        return page.map(post -> mapToResponse(post, currentUser, false));
    }

    public PostResponse getPost(Long id) {
        User currentUser = getCurrentUserOrNull();
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        if (Boolean.TRUE.equals(post.getIsHidden())) {
            throw new RuntimeException("Post not found with id: " + id);
        }
        return mapToResponse(post, currentUser, true);
    }

    public PostResponse updatePost(Long id, PostRequest request) {
        User currentUser = getCurrentUser();
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the author can edit this post");
        }
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        post.setLocation(request.getLocation());
        if (request.getTags() != null) {
            post.setTags(new HashSet<>(request.getTags()));
        }
        Post saved = postRepository.save(post);
        return mapToResponse(saved, currentUser, false);
    }

    public void deletePost(Long id) {
        User currentUser = getCurrentUser();
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        if (!post.getAuthor().getId().equals(currentUser.getId()) && !isAdmin) {
            throw new RuntimeException("Only the author can delete this post");
        }
        postRepository.delete(post);
    }

    public PostResponse likePost(Long id) {
        User currentUser = getCurrentUser();
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        if (!postLikeRepository.existsByPostAndUser(post, currentUser)) {
            PostLike like = new PostLike();
            like.setPost(post);
            like.setUser(currentUser);
            postLikeRepository.save(like);
            post.setLikesCount(post.getLikesCount() + 1);
            post = postRepository.save(post);
        }
        return mapToResponse(post, currentUser, false);
    }

    public PostResponse unlikePost(Long id) {
        User currentUser = getCurrentUser();
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        postLikeRepository.findByPostAndUser(post, currentUser).ifPresent(like -> {
            postLikeRepository.delete(like);
            post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
            postRepository.save(post);
        });
        return mapToResponse(post, currentUser, false);
    }

    public CommentResponse addComment(Long postId, CommentRequest request) {
        User currentUser = getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setAuthor(currentUser);
        comment.setContent(request.getContent());
        if (request.getParentCommentId() != null) {
            PostComment parent = postCommentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found with id: " + request.getParentCommentId()));
            if (!parent.getPost().getId().equals(postId)) {
                throw new RuntimeException("Parent comment does not belong to this post");
            }
            comment.setParentComment(parent);
        }
        PostComment saved = postCommentRepository.save(comment);

        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(post);

        return mapToCommentResponse(saved, currentUser, List.of());
    }

    public void deleteComment(Long commentId) {
        User currentUser = getCurrentUser();
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        if (!comment.getAuthor().getId().equals(currentUser.getId()) && !isAdmin) {
            throw new RuntimeException("Only the author can delete this comment");
        }
        Post post = comment.getPost();
        postCommentRepository.delete(comment);
        post.setCommentsCount(Math.max(0, post.getCommentsCount() - 1));
        postRepository.save(post);
    }

    public PostResponse togglePin(Long id) {
        User currentUser = getCurrentUser();
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new RuntimeException("Only admins can pin posts");
        }
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        post.setIsPinned(!Boolean.TRUE.equals(post.getIsPinned()));
        Post saved = postRepository.save(post);
        return mapToResponse(saved, currentUser, false);
    }

    public PostResponse toggleHide(Long id) {
        User currentUser = getCurrentUser();
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new RuntimeException("Only admins can hide posts");
        }
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        post.setIsHidden(!Boolean.TRUE.equals(post.getIsHidden()));
        Post saved = postRepository.save(post);
        return mapToResponse(saved, currentUser, false);
    }

    private PostResponse mapToResponse(Post post, User currentUser, boolean includeComments) {
        boolean isLiked = currentUser != null && postLikeRepository.existsByPostAndUser(post, currentUser);

        List<CommentResponse> comments = List.of();
        if (includeComments) {
            List<PostComment> allComments = postCommentRepository.findByPostOrderByCreatedAtAsc(post);
            comments = buildCommentTree(allComments, currentUser);
        }

        return PostResponse.builder()
                .id(post.getId())
                .author(mapToUserResponse(post.getAuthor()))
                .content(post.getContent())
                .imageUrls(new ArrayList<>(post.getImageUrls()))
                .location(post.getLocation())
                .tags(new HashSet<>(post.getTags()))
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .sharesCount(0)
                .isLiked(isLiked)
                .createdAt(post.getCreatedAt())
                .timeAgo(timeAgo(post.getCreatedAt()))
                .comments(comments)
                .build();
    }

    private List<CommentResponse> buildCommentTree(List<PostComment> allComments, User currentUser) {
        Map<Long, List<PostComment>> repliesByParentId = allComments.stream()
                .filter(c -> c.getParentComment() != null)
                .collect(Collectors.groupingBy(c -> c.getParentComment().getId()));

        return allComments.stream()
                .filter(c -> c.getParentComment() == null)
                .sorted(Comparator.comparing(PostComment::getCreatedAt))
                .map(c -> mapToCommentResponse(c, currentUser, repliesByParentId.getOrDefault(c.getId(), List.of())))
                .collect(Collectors.toList());
    }

    private CommentResponse mapToCommentResponse(PostComment comment, User currentUser, List<PostComment> replies) {
        List<CommentResponse> replyResponses = replies.stream()
                .sorted(Comparator.comparing(PostComment::getCreatedAt))
                .map(r -> mapToCommentResponse(r, currentUser, List.of()))
                .collect(Collectors.toList());

        return CommentResponse.builder()
                .id(comment.getId())
                .author(mapToUserResponse(comment.getAuthor()))
                .content(comment.getContent())
                .likesCount(comment.getLikesCount())
                .isLiked(false)
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .createdAt(comment.getCreatedAt())
                .timeAgo(timeAgo(comment.getCreatedAt()))
                .replies(replyResponses)
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    private String timeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        long minutes = ChronoUnit.MINUTES.between(dateTime, LocalDateTime.now());
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        long hours = minutes / 60;
        if (hours < 24) return hours + (hours == 1 ? " hour ago" : " hours ago");
        long days = hours / 24;
        if (days < 30) return days + (days == 1 ? " day ago" : " days ago");
        long months = days / 30;
        if (months < 12) return months + (months == 1 ? " month ago" : " months ago");
        long years = months / 12;
        return years + (years == 1 ? " year ago" : " years ago");
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("Authentication required");
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    private User getCurrentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }
}

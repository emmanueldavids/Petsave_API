package com.petsave.petsave.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostResponse {
    
    private Long id;
    
    private UserResponse author;
    
    private String content;
    
    private String imageUrl;
    
    private String location;
    
    private Set<String> tags;
    
    private Integer likesCount;
    
    private Integer commentsCount;
    
    private Integer sharesCount;
    
    private Boolean isLiked;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    private String timeAgo;
    
    private List<CommentResponse> comments;
    
    private List<UserResponse> likedUsers;
    
    // Static factory methods
    public static PostResponse success(String message, PostResponse data) {
        return data;
    }
    
    public static PostResponse error(String message) {
        return null;
    }
}

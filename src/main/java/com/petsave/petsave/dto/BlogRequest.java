package com.petsave.petsave.dto;

import org.springframework.web.multipart.MultipartFile;

public class BlogRequest {

    private String title;
    private String content;
    private String author;
    private MultipartFile image; // For image upload
    

    // Getters and setters
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }

}
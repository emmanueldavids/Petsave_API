package com.petsave.petsave.dto;

import lombok.Data;

@Data
public class ContactRequest {
    private String name;
    private String email;
    private String title;
    private String message;
}

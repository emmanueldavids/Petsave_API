package com.petsave.petsave.Config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> response = Map.of(
            "message", ex.getMessage(),
            "status", "error"
        );
        
        // Determine HTTP status based on message
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = ex.getMessage().toLowerCase();
        
        if (message.contains("email already exists") || message.contains("username already exists")) {
            status = HttpStatus.CONFLICT;
        } else if (message.contains("user not found") || message.contains("invalid")) {
            status = HttpStatus.NOT_FOUND;
        } else if (message.contains("unauthorized") || message.contains("authentication failed")) {
            status = HttpStatus.UNAUTHORIZED;
        }
        
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> response = Map.of(
            "message", "An unexpected error occurred. Please try again.",
            "status", "error"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

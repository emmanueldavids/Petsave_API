package com.petsave.petsave.Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Slf4j
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
        } else if (message.contains("authentication failed") || message.contains("refresh token") || message.contains("unauthorized")) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (message.contains("user not found") || message.contains("invalid")) {
            status = HttpStatus.NOT_FOUND;
        }
        
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        Map<String, String> response = Map.of(
            "message", "An unexpected error occurred. Please try again.",
            "status", "error"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxUpload(MaxUploadSizeExceededException ex) {
        Map<String, String> response = Map.of(
            "message", "Upload too large. Maximum allowed size is 5MB.",
            "status", "error"
        );
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUsernameNotFound(UsernameNotFoundException ex) {
        Map<String, String> response = Map.of(
            "message", "Authentication failed. Please log in again.",
            "status", "error"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}

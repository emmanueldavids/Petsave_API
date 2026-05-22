package com.petsave.petsave.Controller;

import com.petsave.petsave.Service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@Slf4j
public class FileUploadController {

    @Autowired
    private CloudinaryService cloudinaryService;

    private final String MAX_FILE_SIZE = "10MB";
    private final long MAX_SIZE = 10 * 1024 * 1024; // 10MB

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("POST /api/upload/image - Uploading image to Cloudinary: {}", file.getOriginalFilename());
        
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please select a file to upload"));
            }
            
            // Check file size
            if (file.getSize() > MAX_SIZE) {
                return ResponseEntity.badRequest().body(Map.of("error", "File size exceeds maximum limit of " + MAX_FILE_SIZE));
            }
            
            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only image files are allowed"));
            }
            
            // Upload to Cloudinary
            String cloudinaryUrl = cloudinaryService.uploadPetImage(file);
            
            Map<String, String> response = new HashMap<>();
            response.put("filename", file.getOriginalFilename());
            response.put("originalName", file.getOriginalFilename());
            response.put("url", cloudinaryUrl);
            response.put("size", String.valueOf(file.getSize()));
            response.put("contentType", contentType);
            
            log.info("Image uploaded successfully to Cloudinary: {}", cloudinaryUrl);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error uploading file to Cloudinary: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }
    }

    @PostMapping("/file")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("POST /api/upload/file - Uploading file to Cloudinary: {}", file.getOriginalFilename());
        
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please select a file to upload"));
            }
            
            // Check file size
            if (file.getSize() > MAX_SIZE) {
                return ResponseEntity.badRequest().body(Map.of("error", "File size exceeds maximum limit of " + MAX_FILE_SIZE));
            }
            
            // Upload to Cloudinary
            String cloudinaryUrl = cloudinaryService.uploadFile(file, "general-files");
            
            Map<String, String> response = new HashMap<>();
            response.put("filename", file.getOriginalFilename());
            response.put("originalName", file.getOriginalFilename());
            response.put("url", cloudinaryUrl);
            response.put("size", String.valueOf(file.getSize()));
            response.put("contentType", file.getContentType());
            
            log.info("File uploaded successfully to Cloudinary: {}", cloudinaryUrl);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error uploading file to Cloudinary: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }
    }
}

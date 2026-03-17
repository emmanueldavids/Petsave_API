package com.petsave.petsave.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class FileUploadController {

    private final String UPLOAD_DIR = "uploads/";
    private final String MAX_FILE_SIZE = "10MB";
    private final long MAX_SIZE = 10 * 1024 * 1024; // 10MB

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("POST /api/upload/image - Uploading image: {}", file.getOriginalFilename());
        
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
            
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            
            // Save file
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath);
            
            // Return file URL
            String fileUrl = "/uploads/" + uniqueFilename;
            
            Map<String, String> response = new HashMap<>();
            response.put("filename", uniqueFilename);
            response.put("originalName", originalFilename);
            response.put("url", fileUrl);
            response.put("size", String.valueOf(file.getSize()));
            response.put("contentType", contentType);
            
            log.info("Image uploaded successfully: {}", fileUrl);
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Error uploading file: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to upload file"));
        }
    }

    @PostMapping("/file")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("POST /api/upload/file - Uploading file: {}", file.getOriginalFilename());
        
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please select a file to upload"));
            }
            
            // Check file size
            if (file.getSize() > MAX_SIZE) {
                return ResponseEntity.badRequest().body(Map.of("error", "File size exceeds maximum limit of " + MAX_FILE_SIZE));
            }
            
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            
            // Save file
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath);
            
            // Return file URL
            String fileUrl = "/uploads/" + uniqueFilename;
            
            Map<String, String> response = new HashMap<>();
            response.put("filename", uniqueFilename);
            response.put("originalName", originalFilename);
            response.put("url", fileUrl);
            response.put("size", String.valueOf(file.getSize()));
            response.put("contentType", file.getContentType());
            
            log.info("File uploaded successfully: {}", fileUrl);
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Error uploading file: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to upload file"));
        }
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<byte[]> getFile(@PathVariable String filename) {
        log.info("GET /api/upload/files/{} - Serving file", filename);
        
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(filename);
            
            if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] fileContent = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            
            return ResponseEntity.ok()
                    .header("Content-Type", contentType != null ? contentType : "application/octet-stream")
                    .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                    .body(fileContent);
                    
        } catch (IOException e) {
            log.error("Error serving file: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}

package com.petsave.petsave.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadFile(MultipartFile file, String folder) {
        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "auto",
                            "quality", "auto",
                            "fetch_format", "auto"
                    )
            );
            
            String publicId = (String) uploadResult.get("public_id");
            String url = (String) uploadResult.get("secure_url");
            
            log.info("Successfully uploaded file to Cloudinary. Public ID: {}, URL: {}", publicId, url);
            return url;
        } catch (IOException e) {
            log.error("Error uploading file to Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public String uploadPetImage(MultipartFile file) {
        return uploadFile(file, "pet-images");
    }

    public boolean deleteFile(String publicId) {
        try {
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String resultString = (String) result.get("result");
            boolean deleted = "ok".equals(resultString);
            
            if (deleted) {
                log.info("Successfully deleted file from Cloudinary. Public ID: {}", publicId);
            } else {
                log.warn("Failed to delete file from Cloudinary. Public ID: {}, Result: {}", publicId, resultString);
            }
            
            return deleted;
        } catch (Exception e) {
            log.error("Error deleting file from Cloudinary: {}", e.getMessage());
            return false;
        }
    }
}

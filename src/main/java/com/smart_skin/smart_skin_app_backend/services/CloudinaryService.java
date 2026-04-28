package com.smart_skin.smart_skin_app_backend.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.smart_skin.smart_skin_app_backend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file, String folder) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "smart_skin/" + folder,
                            "resource_type", "image",
                            "quality", "auto",
                            "fetch_format", "auto"
                    )
            );
            return (String) result.get("secure_url");
        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary: {}", e.getMessage());
            throw new BadRequestException("Échec du téléversement de l'image");
        }
    }

    public Map<String, String> uploadImageWithDetails(MultipartFile file, String folder) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "smart_skin/" + folder,
                            "resource_type", "image",
                            "quality", "auto",
                            "fetch_format", "auto"
                    )
            );
            return Map.of(
                    "url", (String) result.get("secure_url"),
                    "publicId", (String) result.get("public_id"),
                    "format", (String) result.get("format"),
                    "thumbnailUrl", buildThumbnailUrl((String) result.get("secure_url"))
            );
        } catch (IOException e) {
            log.error("Failed to upload image: {}", e.getMessage());
            throw new BadRequestException("Échec du téléversement de l'image");
        }
    }

    public void deleteImage(String imageUrl) {
        try {
            String publicId = extractPublicId(imageUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (IOException e) {
            log.warn("Failed to delete image from Cloudinary: {}", e.getMessage());
        }
    }

    private String extractPublicId(String imageUrl) {
        if (imageUrl == null) return null;
        try {
            // Extract public_id from URL: .../smart_skin/folder/filename.ext
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) return null;
            String path = parts[1];
            // Remove version prefix (v12345/) if present
            if (path.matches("v\\d+/.*")) {
                path = path.substring(path.indexOf('/') + 1);
            }
            // Remove extension
            return path.substring(0, path.lastIndexOf('.'));
        } catch (Exception e) {
            return null;
        }
    }

    private String buildThumbnailUrl(String originalUrl) {
        // Insert Cloudinary transformation for thumbnail (200x200, cropped)
        return originalUrl.replace("/upload/", "/upload/w_200,h_200,c_fill/");
    }
}

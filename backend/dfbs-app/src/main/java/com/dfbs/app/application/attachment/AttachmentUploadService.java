package com.dfbs.app.application.attachment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Handles file upload with size validation. MVP: mock storage returns a mock URL.
 */
@Service
public class AttachmentUploadService {

    public static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024; // 10MB

    @Value("${dfbs.attachment.mock-base-url:/uploads}")
    private String mockBaseUrl;

    /**
     * Validate size and "store" file (MVP: mock). Returns URL and original filename.
     *
     * @param file           Uploaded file
     * @param attachmentType Type for categorization
     * @return UploadResult with url and name
     * @throws IllegalArgumentException if file is null, empty, or exceeds 10MB
     */
    public UploadResult upload(MultipartFile file, AttachmentType attachmentType) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File size must not exceed 10MB.");
        }
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String ext = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf('.')) : "";
        String mockPath = mockBaseUrl + "/" + attachmentType.name().toLowerCase() + "/" + UUID.randomUUID() + ext;
        return new UploadResult(mockPath, originalFilename);
    }

    public record UploadResult(String url, String name) {}
}

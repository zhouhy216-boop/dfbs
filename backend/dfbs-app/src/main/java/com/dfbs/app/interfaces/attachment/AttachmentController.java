package com.dfbs.app.interfaces.attachment;

import com.dfbs.app.application.attachment.AttachmentType;
import com.dfbs.app.application.attachment.AttachmentUploadService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/attachments")
public class AttachmentController {

    private final AttachmentUploadService uploadService;

    public AttachmentController(AttachmentUploadService uploadService) {
        this.uploadService = uploadService;
    }

    /**
     * Upload a file. Max size 10MB. Returns url and name.
     * @param file           The file to upload
     * @param attachmentType One of: BILL_PHOTO, PICK_TICKET, RECEIPT, LOGISTICS_BILL, DAMAGE_PHOTO
     */
    @PostMapping("/upload")
    public UploadResponse upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "attachmentType", defaultValue = "BILL_PHOTO") String attachmentType) {
        AttachmentType type;
        try {
            type = AttachmentType.valueOf(attachmentType != null ? attachmentType.toUpperCase() : "BILL_PHOTO");
        } catch (IllegalArgumentException e) {
            type = AttachmentType.BILL_PHOTO;
        }
        var result = uploadService.upload(file, type);
        return new UploadResponse(result.url(), result.name());
    }

    public record UploadResponse(String url, String name) {}
}

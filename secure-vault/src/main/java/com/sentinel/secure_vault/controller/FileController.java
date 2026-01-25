package com.sentinel.secure_vault.controller;

import com.sentinel.secure_vault.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    // 1. UPLOAD ENDPOINT
    // Automatically grabs the email from the logged-in user (Token)
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        try {
            String email = authentication.getName(); // Extract email from Token
            String fileId = fileService.storeFile(file, email);
            return ResponseEntity.ok("File uploaded successfully! ID: " + fileId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }

    // 2. DOWNLOAD ENDPOINT
    // Automatically checks if the logged-in user has permission
    @GetMapping("/download/{id}")
    public ResponseEntity<ByteArrayResource> downloadFile(
            @PathVariable Long id,
            Authentication authentication
    ) {

        try {
            String requesterEmail = authentication.getName(); // Who is asking?

            // Call the Service (The Service handles the Security Check now)
            byte[] data = fileService.downloadFile(id, requesterEmail);

            String contentType = fileService.getContentType(id);
            String originalName = fileService.getOriginalFileName(id);

            ByteArrayResource resource = new ByteArrayResource(data);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalName + "\"")
                    .body(resource);
            // ... inside downloadFile method ...
        }  catch (Exception e) {
         return ResponseEntity.status(403).build();
    }
    }

    // 3. SHARE ENDPOINT
    // Allows the owner to share a file with someone else
    @PostMapping("/share")
    public ResponseEntity<String> shareFile(
            @RequestParam Long fileId,
            @RequestParam String email, // The person receiving the file
            Authentication authentication // The Owner (sender)
    ) {
        try {
            String ownerEmail = authentication.getName();
            String response = fileService.shareFile(fileId, ownerEmail, email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Share failed: " + e.getMessage());
        }
    }
}
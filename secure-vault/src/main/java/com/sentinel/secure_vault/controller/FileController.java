package com.sentinel.secure_vault.controller;

import com.sentinel.secure_vault.model.SecureFile;
import com.sentinel.secure_vault.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    // 1. UPLOAD ENDPOINT
    // 1. UPLOAD ENDPOINT
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        try {
            String email = authentication.getName();
            // Call service
            String fileId = fileService.storeFile(file, email);

            System.out.println("✅ UPLOAD SUCCESS: " + fileId); // Log success
            return ResponseEntity.ok("File uploaded successfully! ID: " + fileId);

        } catch (Exception e) {
            // 🛑 Log the specific error if it fails
            System.out.println("❌ UPLOAD FAILED: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
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

    @GetMapping
    public ResponseEntity<List<SecureFile>> getMyFiles(Authentication authentication) {
        String email = authentication.getName();
        List<SecureFile> files = fileService.getAllFiles(email);
        return ResponseEntity.ok(files);
    }

    // 2. STREAM/VIEW ENDPOINT
    @GetMapping("/download/{id}")
    public ResponseEntity<ByteArrayResource> streamFile(
            @PathVariable Long id,
            Authentication authentication
    ) {
        try {
            String requesterEmail = authentication.getName();
            // Call Service
            byte[] data = fileService.downloadFile(id, requesterEmail);

            String contentType = fileService.getContentType(id);
            ByteArrayResource resource = new ByteArrayResource(data);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(resource);

        } catch (Exception e) {
            // 🛑 THIS IS THE FIX: Print the real error to the IntelliJ Console
            System.out.println("❌ ERROR STREAMING FILE: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(403).body(null);
        }
    }

    // 5. DOWNLOAD ENCRYPTED .SNTL (The Token)
    // This endpoint AUTOMATICALLY stamps the ID into the filename
    @GetMapping("/download-sntl/{id}")
    public ResponseEntity<ByteArrayResource> downloadEncryptedFile(
            @PathVariable Long id,
            Authentication authentication
    ) {
        try {
            String requesterEmail = authentication.getName();

            // 1. Get the raw encrypted bytes
            byte[] data = fileService.downloadEncryptedSntl(id, requesterEmail);

            // 2. Get the original name (e.g., "MiniProject.pdf")
            String originalName = fileService.getOriginalFileName(id);

            // 🛑 CRITICAL FIX: AUTOMATIC STAMPING
            // We force the filename to include "_id_{number}"
            // Result: "MiniProject.pdf_id_15.sntl"
            String sntlName = originalName + "_id_" + id + ".sntl";

            ByteArrayResource resource = new ByteArrayResource(data);

            return ResponseEntity.ok()
                    // This header tells the browser: "Save the file with THIS specific name"
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + sntlName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (Exception e) {
            System.out.println("❌ ERROR DOWNLOADING SNTL: " + e.getMessage());
            return ResponseEntity.status(403).body(null);
        }
    }
    // 6. GET ACCESS LIST (Owner Only)
    @GetMapping("/shares/{fileId}")
    public ResponseEntity<?> getFileShares(@PathVariable Long fileId, Authentication authentication) {
        try {
            String ownerEmail = authentication.getName();
            List<String> sharedEmails = fileService.getFileShares(fileId, ownerEmail);
            return ResponseEntity.ok(sharedEmails);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ... existing code ...

    // 7. REVOKE ACCESS
    @DeleteMapping("/revoke")
    public ResponseEntity<String> revokeAccess(
            @RequestParam Long fileId,
            @RequestParam String email,
            Authentication authentication) {
        try {
            String response = fileService.revokeAccess(fileId, authentication.getName(), email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 8. DELETE ENDPOINT
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable Long id, Authentication authentication) {
        try {
            String response = fileService.deleteFile(id, authentication.getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Return 403 Forbidden if not owner, or 400 for other errors
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }



}
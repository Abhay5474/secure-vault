package com.sentinel.secure_vault.service;

import com.sentinel.secure_vault.model.FileShare;
import com.sentinel.secure_vault.model.SecureFile;
import com.sentinel.secure_vault.model.User;
import com.sentinel.secure_vault.repository.FileRepository;
import com.sentinel.secure_vault.repository.FileShareRepository;
import com.sentinel.secure_vault.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileService {

    private final String UPLOAD_DIR = "secure_uploads/"; // Folder on your laptop

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FileShareRepository fileShareRepository;

    // (Ensure UserRepository is also autowired if not already)

    @Autowired
    private EncryptionService encryptionService;

    public String storeFile(MultipartFile file, String ownerEmail) throws Exception {
        // 1. Create the upload folder if it doesn't exist
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 2. Generate a unique filename for storage (to avoid name collisions)
        // We use UUID so if two people upload "test.pdf", they don't overwrite each other.
        String uniqueFileName = UUID.randomUUID().toString() + ".sntl";
        String filePath = UPLOAD_DIR + uniqueFileName;

        // 3. Generate a fresh AES Key for this specific file
        SecretKey key = encryptionService.generateKey();

        // 4. Encrypt the file data
        byte[] encryptedData = encryptionService.encrypt(file.getBytes(), key);

        // 5. Save the Encrypted File to Disk
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(encryptedData);
        }

        // 6. Save Metadata + Key to Database
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SecureFile secureFile = new SecureFile();
        secureFile.setFileName(file.getOriginalFilename());
        secureFile.setFileType(file.getContentType());
        secureFile.setStoragePath(filePath);
        secureFile.setOwner(owner);
        secureFile.setEncryptedKey(encryptionService.encodeKey(key)); // Store key as String

        fileRepository.save(secureFile);

        return "File uploaded and encrypted successfully! ID: " + secureFile.getId();
    }
    // ... inside FileService class ...

    // Updated: Now requires the email of the person asking for the file
    public byte[] downloadFile(Long fileId, String requesterEmail) throws Exception {


        // 1. Find the File
        SecureFile fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // 2. Authorization Check (The "Bouncer")
        boolean isOwner = fileEntity.getOwner().getEmail().equals(requesterEmail);

        // We use the Repository to check if a permission slip exists for this user
        boolean isShared = fileShareRepository.existsByFileIdAndSharedToEmail(fileId, requesterEmail);

        if (!isOwner && !isShared) {
            throw new RuntimeException("ACCESS DENIED: You do not have permission to view this file.");
        }

        // 3. If passed, proceed to Decrypt
        java.io.File fileOnDisk = new java.io.File(fileEntity.getStoragePath());
        byte[] encryptedBytes = java.nio.file.Files.readAllBytes(fileOnDisk.toPath());
        javax.crypto.SecretKey key = encryptionService.decodeKey(fileEntity.getEncryptedKey());
        return encryptionService.decrypt(encryptedBytes, key);
    }
    // Helper to get the Content Type (so the browser knows it's a PDF)
    public String getContentType(Long fileId) {
        return fileRepository.findById(fileId)
                .map(SecureFile::getFileType)
                .orElse("application/octet-stream");
    }
    // ... inside FileService ...

    public String getOriginalFileName(Long fileId) {
        return fileRepository.findById(fileId)
                .map(SecureFile::getFileName) // This fetches "report.pdf" from DB
                .orElse("unknown_file");
    }
    // ... inside FileService ...

    public String shareFile(Long fileId, String ownerEmail, String shareWithEmail) {
        // 1. Validate: Cannot share with yourself
        if (ownerEmail.equals(shareWithEmail)) {
            throw new RuntimeException("You cannot share a file with yourself!");
        }

        // 2. Find the File AND ensure the requester is the Owner
        SecureFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Not authorized: You are not the owner of this file.");
        }

        // 3. Find the user we want to share with
        User receiver = userRepository.findByEmail(shareWithEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + shareWithEmail));

        // 4. Check if already shared
        if (fileShareRepository.existsByFileIdAndSharedToEmail(fileId, shareWithEmail)) {
            return "File is already shared with this user.";
        }

        // 5. Create the Permission Slip
        FileShare share = new FileShare();
        share.setFile(file);
        share.setSharedTo(receiver);
        fileShareRepository.save(share);

        return "File shared successfully with " + shareWithEmail;
    }
}
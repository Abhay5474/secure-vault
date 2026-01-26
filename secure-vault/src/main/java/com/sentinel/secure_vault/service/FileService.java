package com.sentinel.secure_vault.service;

import com.sentinel.secure_vault.model.FileShare;
import com.sentinel.secure_vault.model.SecureFile;
import com.sentinel.secure_vault.model.User;
import com.sentinel.secure_vault.repository.FileRepository;
import com.sentinel.secure_vault.repository.FileShareRepository;
import com.sentinel.secure_vault.repository.UserRepository;
import com.sentinel.secure_vault.util.AESUtil; // ✅ Added Import

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileService {

    private final String UPLOAD_DIR = "secure_uploads/";

    @Autowired
    private FileRepository fileRepository;


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileShareRepository fileShareRepository;

    @Autowired
    private EncryptionService encryptionService;

    // 1. STORE FILE (Owner Creates .sntl)
    public String storeFile(MultipartFile file, String ownerEmail) throws Exception {
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) directory.mkdirs();

        String uniqueFileName = UUID.randomUUID().toString() + ".sntl";
        String filePath = UPLOAD_DIR + uniqueFileName;

        // Generate Key & Encrypt
        SecretKey key = encryptionService.generateKey();
        byte[] encryptedData = encryptionService.encrypt(file.getBytes(), key);

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(encryptedData);
        }

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SecureFile secureFile = new SecureFile();
        secureFile.setFileName(file.getOriginalFilename());
        secureFile.setFileType(file.getContentType());
        secureFile.setStoragePath(filePath);
        secureFile.setOwner(owner);
        secureFile.setEncryptedKey(encryptionService.encodeKey(key));

        fileRepository.save(secureFile);
        return "File encrypted and stored. ID: " + secureFile.getId();
    }



    // ... (rest of the code) ...

    // 3. DOWNLOAD / STREAM FILE (For Viewer)
    public byte[] downloadFile(Long fileId, String requesterEmail) throws Exception {
        // 1. Find the file
        SecureFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // 2. Permission Check (Owner OR Shared)
        boolean isOwner = file.getOwner().getEmail().equalsIgnoreCase(requesterEmail);
        boolean isShared = fileShareRepository.existsByFile_IdAndSharedWith_Email(fileId, requesterEmail);

        if (!isOwner && !isShared) {
            System.out.println("❌ ACCESS DENIED for: " + requesterEmail);
            throw new RuntimeException("ACCESS DENIED: The owner has not granted you permission.");
        }

        // 3. Read Encrypted Bytes from Disk
        File fileOnDisk = new File(file.getStoragePath());
        if (!fileOnDisk.exists()) {
            fileOnDisk = new File(System.getProperty("user.dir"), file.getStoragePath());
        }

        if (!fileOnDisk.exists()) {
            throw new RuntimeException("Encrypted file not found on server disk.");
        }

        byte[] encryptedContent = Files.readAllBytes(fileOnDisk.toPath());

        // 4. FIX: Use EncryptionService (NOT AESUtil)
        // This ensures the Math used to lock the file is the same Math used to unlock it.
        return encryptionService.decrypt(encryptedContent, file.getEncryptedKey());
    }

    // LIST SHARES
    public List<String> getFileShares(Long fileId, String ownerEmail) {
        SecureFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Access Denied: You are not the owner.");
        }

        // Return list of emails
        return fileShareRepository.findByFile_Id(fileId).stream()
                .map(share -> share.getSharedWith().getEmail())
                .collect(Collectors.toList());
    }

    // SHARE FILE (Updated for Robustness)
    public String shareFile(Long fileId, String ownerEmail, String shareWithEmail) {
        // Normalize email to lowercase to prevent mismatch
        String targetEmail = shareWithEmail.toLowerCase().trim();

        if (ownerEmail.equals(targetEmail)) throw new RuntimeException("You already own this file.");

        SecureFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Only the owner can share permissions.");
        }

        // 🛑 CHECK: User MUST be registered
        User receiver = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new RuntimeException("User '" + targetEmail + "' is not registered in our system."));

        // Check if already shared
        if (fileShareRepository.existsByFile_IdAndSharedWith_Email(fileId, targetEmail)) {
            return "File is already shared with " + targetEmail;
        }

        FileShare share = new FileShare();
        share.setFile(file);
        share.setSharedWith(receiver);
        fileShareRepository.save(share);

        return "Access granted to " + targetEmail;
    }

    // 4. LIST FILES (OWNER ONLY - Hides Shared Files)
    public List<SecureFile> getAllFiles(String email) {
        return fileRepository.findByOwner_Email(email);
    }

    // 5. DOWNLOAD .SNTL (Raw Encrypted)
    public byte[] downloadEncryptedSntl(Long fileId, String requesterEmail) throws Exception {
        SecureFile fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // Permissions Check (Owner OR Shared)
        boolean isOwner = fileEntity.getOwner().getEmail().equalsIgnoreCase(requesterEmail);
        boolean isShared = fileShareRepository.existsByFile_IdAndSharedWith_Email(fileId, requesterEmail);

        if (!isOwner && !isShared) {
            throw new RuntimeException("ACCESS DENIED");
        }

        File fileOnDisk = new File(fileEntity.getStoragePath());
        if (!fileOnDisk.exists()) {
            fileOnDisk = new File(System.getProperty("user.dir"), fileEntity.getStoragePath());
            if (!fileOnDisk.exists()) throw new RuntimeException("File missing on disk");
        }

        return Files.readAllBytes(fileOnDisk.toPath());
    }

    public String getContentType(Long fileId) {
        return fileRepository.findById(fileId).map(SecureFile::getFileType).orElse("application/octet-stream");
    }

    public String getOriginalFileName(Long fileId) {
        return fileRepository.findById(fileId).map(SecureFile::getFileName).orElse("unknown");
    }
    // ... existing code ...

    // 7. REVOKE ACCESS (Owner Only)
    // @Transactional is required for delete operations
    @org.springframework.transaction.annotation.Transactional
    public String revokeAccess(Long fileId, String ownerEmail, String targetEmail) {
        SecureFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getOwner().getEmail().equalsIgnoreCase(ownerEmail)) {
            throw new RuntimeException("Only the owner can revoke permissions.");
        }

        // Delete the share record
        fileShareRepository.deleteByFile_IdAndSharedWith_Email(fileId, targetEmail);

        return "Access revoked for " + targetEmail;
    }

    // 8. PERMANENT DELETE (Owner Only)
    @org.springframework.transaction.annotation.Transactional
    public String deleteFile(Long fileId, String ownerEmail) {
        SecureFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // Security Check: Only Owner can delete
        if (!file.getOwner().getEmail().equalsIgnoreCase(ownerEmail)) {
            throw new RuntimeException("ACCESS DENIED: Only the owner can delete this file.");
        }

        try {
            // STEP 1: Delete all permission records (Shares) first
            // This prevents "Foreign Key Constraint" errors in the database
            List<FileShare> shares = fileShareRepository.findByFile_Id(fileId);
            fileShareRepository.deleteAll(shares);

            // STEP 2: Delete the physical file from the "secure_uploads" folder
            File fileOnDisk = new File(file.getStoragePath());
            if (fileOnDisk.exists()) {
                if (fileOnDisk.delete()) {
                    System.out.println("✅ Physical file deleted: " + file.getFileName());
                } else {
                    System.err.println("⚠️ Warning: Could not delete physical file (might be open or missing).");
                }
            }

            // STEP 3: Delete the File Record from Database
            fileRepository.delete(file);

            return "File deleted successfully.";

        } catch (Exception e) {
            throw new RuntimeException("Error deleting file: " + e.getMessage());
        }
    }
}
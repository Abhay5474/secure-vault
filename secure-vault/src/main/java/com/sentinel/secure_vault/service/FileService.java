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
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileService {

    // ❌ REMOVED: private final String UPLOAD_DIR... (Not needed for DB storage)

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileShareRepository fileShareRepository;

    @Autowired
    private EncryptionService encryptionService;

    // 1. STORE FILE (Owner Creates .sntl -> Saved to DB)
    public String storeFile(MultipartFile file, String ownerEmail) throws Exception {

        // Generate Key & Encrypt the raw bytes
        SecretKey key = encryptionService.generateKey();
        byte[] encryptedData = encryptionService.encrypt(file.getBytes(), key);

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SecureFile secureFile = new SecureFile();
        secureFile.setFileName(file.getOriginalFilename());
        secureFile.setFileType(file.getContentType());
        secureFile.setOwner(owner);
        secureFile.setEncryptedKey(encryptionService.encodeKey(key));

        // ✅ NEW: Save encrypted bytes DIRECTLY into the Database
        // This prevents files from being deleted when Render restarts.
        secureFile.setFileData(encryptedData);

        fileRepository.save(secureFile);
        return "File encrypted and stored in Database. ID: " + secureFile.getId();
    }

    // 3. DOWNLOAD / STREAM FILE (Decrypts from DB)
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

        // 3. Read Encrypted Bytes from Database Entity (Not Disk)
        byte[] encryptedContent = file.getFileData();

        if (encryptedContent == null || encryptedContent.length == 0) {
            throw new RuntimeException("File content is empty or corrupted in database.");
        }

        // 4. Decrypt using the stored key
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

    // SHARE FILE
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
        // Note: The fileData BLOB is lazy loaded usually, so this list won't crash your memory
        return fileRepository.findByOwner_Email(email);
    }

    // 5. DOWNLOAD .SNTL (Raw Encrypted from DB)
    public byte[] downloadEncryptedSntl(Long fileId, String requesterEmail) throws Exception {
        SecureFile fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // Permissions Check (Owner OR Shared)
        boolean isOwner = fileEntity.getOwner().getEmail().equalsIgnoreCase(requesterEmail);
        boolean isShared = fileShareRepository.existsByFile_IdAndSharedWith_Email(fileId, requesterEmail);

        if (!isOwner && !isShared) {
            throw new RuntimeException("ACCESS DENIED");
        }

        // Return raw encrypted bytes from DB
        return fileEntity.getFileData();
    }

    public String getContentType(Long fileId) {
        return fileRepository.findById(fileId).map(SecureFile::getFileType).orElse("application/octet-stream");
    }

    public String getOriginalFileName(Long fileId) {
        return fileRepository.findById(fileId).map(SecureFile::getFileName).orElse("unknown");
    }

    // 7. REVOKE ACCESS (Owner Only)
    @Transactional
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
    @Transactional
    public String deleteFile(Long fileId, String ownerEmail) {
        SecureFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // Security Check: Only Owner can delete
        if (!file.getOwner().getEmail().equalsIgnoreCase(ownerEmail)) {
            throw new RuntimeException("ACCESS DENIED: Only the owner can delete this file.");
        }

        try {
            // STEP 1: Delete all permission records (Shares) first
            List<FileShare> shares = fileShareRepository.findByFile_Id(fileId);
            fileShareRepository.deleteAll(shares);

            // STEP 2: Delete from Database (This also deletes the BLOB data)
            // ❌ Removed disk deletion logic since file is now inside the DB row
            fileRepository.delete(file);

            return "File deleted successfully.";

        } catch (Exception e) {
            throw new RuntimeException("Error deleting file: " + e.getMessage());
        }
    }
}
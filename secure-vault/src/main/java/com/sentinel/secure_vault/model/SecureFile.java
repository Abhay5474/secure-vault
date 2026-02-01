package com.sentinel.secure_vault.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "secure_files")
@Data
public class SecureFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The original filename (e.g., "report.pdf")
    @Column(nullable = false)
    private String fileName;

    // The file type (e.g., "application/pdf")
    @Column(nullable = false)
    private String fileType;

    // ❌ REMOVED: private String storagePath;
    // (We don't trust the disk anymore)

    // ✅ NEW: Store the actual encrypted file bytes inside the database
    // "LONGBLOB" allows storing large files (up to 4GB in MySQL/TiDB)
    @Lob
    @Column(name = "file_data", columnDefinition = "LONGBLOB")
    private byte[] fileData;

    // ⚠️ THE KEY: Stored as a Base64 String.
    @Column(nullable = false, length = 512)
    private String encryptedKey;

    // Who owns this file?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    private LocalDateTime uploadTime;

    @PrePersist // Automatically sets the time before saving to DB
    protected void onCreate() {
        uploadTime = LocalDateTime.now();
    }
}
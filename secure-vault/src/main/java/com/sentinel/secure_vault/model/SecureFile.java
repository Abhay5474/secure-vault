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

    // The file type (e.g., "application/pdf") - Important for the browser to render it later
    @Column(nullable = false)
    private String fileType;

    // The path where the encrypted bytes are saved on your laptop
    // Example: "uploads/a1b2-c3d4-e5f6.sntl"
    @Column(nullable = false, unique = true)
    private String storagePath;

    // ⚠️ THE KEY: Stored as a Base64 String.
    // In a real startup, we would encrypt this column too (Key Wrapping),
    // but for this project, storing it here allows us to decrypt the file when requested.
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
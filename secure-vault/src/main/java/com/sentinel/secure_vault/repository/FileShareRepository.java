package com.sentinel.secure_vault.repository;

import com.sentinel.secure_vault.model.FileShare;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FileShareRepository extends JpaRepository<FileShare, Long> {

    // 1. Used for Dashboard: Find all files shared with this email
    List<FileShare> findBySharedWith_Email(String email);

    // 2. Used for Download Security: Check if a specific share exists
    // "Does a record exist for this File ID and this Recipient Email?"
    boolean existsByFile_IdAndSharedWith_Email(Long fileId, String email);
    List<FileShare> findByFile_Id(Long fileId);
    // NEW: Find a specific share record to delete it
    void deleteByFile_IdAndSharedWith_Email(Long fileId, String email);
}
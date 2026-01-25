package com.sentinel.secure_vault.repository;

import com.sentinel.secure_vault.model.FileShare;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FileShareRepository extends JpaRepository<FileShare, Long> {

    // Check if a specific share already exists (to prevent duplicates)
    boolean existsByFileIdAndSharedToEmail(Long fileId, String email);

    // Security Check: Does this user have a permission slip for this file?
    boolean existsByFileIdAndSharedToId(Long fileId, Long userId);
}
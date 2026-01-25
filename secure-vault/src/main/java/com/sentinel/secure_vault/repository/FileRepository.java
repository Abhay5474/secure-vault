package com.sentinel.secure_vault.repository;

import com.sentinel.secure_vault.model.SecureFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FileRepository extends JpaRepository<SecureFile, Long> {
    // Custom query to find all files belonging to a specific user
    List<SecureFile> findAllByOwnerId(Long ownerId);
}
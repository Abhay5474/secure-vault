package com.sentinel.secure_vault.repository;

import com.sentinel.secure_vault.model.SecureFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FileRepository extends JpaRepository<SecureFile, Long> {
    // Finds all files where the 'owner' user has this 'email'
    List<SecureFile> findByOwner_Email(String email);
}
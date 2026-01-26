package com.sentinel.secure_vault.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "file_shares")
@Data
public class FileShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which file is being shared?
    @ManyToOne
    @JoinColumn(name = "file_id", nullable = false)
    private SecureFile file;

    // Who is it shared WITH?
    // RENAMED from 'sharedTo' to 'sharedWith' to match Repository & Service
    @ManyToOne
    @JoinColumn(name = "shared_to_user_id", nullable = false)
    private User sharedWith;
}
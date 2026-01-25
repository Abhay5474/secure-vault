package com.sentinel.secure_vault.dto;

import lombok.Data;

@Data // Generates Getters/Setters automatically
public class UserRegistrationDto {
    private String email;
    private String password;
    private String role; // "ADMIN" or "USER"
}
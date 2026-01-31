package com.sentinel.secure_vault.controller;

import com.sentinel.secure_vault.dto.UserRegistrationDto;
import com.sentinel.secure_vault.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // Tells Spring: "This is a REST API handler"
@RequestMapping("/api/auth") // Base URL for all endpoints in this class
public class AuthController {

    @Autowired
    private UserService userService;

    // POST /api/auth/register
    @PostMapping("/register")
    public String register(@RequestBody UserRegistrationDto request) {
        // We pass the work to the Service
        return userService.registerUser(request);
    }
    // ... inside AuthController ...

    // POST /api/auth/login
    @PostMapping("/login")
    public String login(@RequestBody UserRegistrationDto request) {
        return userService.login(request.getEmail(), request.getPassword());

    }
    // FORGOT PASSWORD REQUEST
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        try {
            String response = userService.forgotPassword(email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // RESET PASSWORD SUBMIT
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        try {
            String response = userService.resetPassword(token, newPassword);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
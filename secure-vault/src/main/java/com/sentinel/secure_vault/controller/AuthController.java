package com.sentinel.secure_vault.controller;

import com.sentinel.secure_vault.dto.UserRegistrationDto;
import com.sentinel.secure_vault.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
}
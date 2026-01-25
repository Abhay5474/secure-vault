package com.sentinel.secure_vault.service;

import com.sentinel.secure_vault.dto.UserRegistrationDto;
import com.sentinel.secure_vault.model.User;
import com.sentinel.secure_vault.repository.UserRepository;
import com.sentinel.secure_vault.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service // Tells Spring: "This holds business logic"
public class UserService {

    @Autowired // Dependency Injection: Spring automatically gives us the Repository
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder; // Inject the scrambler

    public String registerUser(UserRegistrationDto request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return "Error: Email already taken!";
        }

        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setRole(request.getRole());

        // OLD: newUser.setPassword(request.getPassword());
        // NEW: Hash it!
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(newUser);
        return "User registered successfully!";
    }
    // ... inside UserService ...

    @Autowired
    private JwtUtil jwtUtil; // Inject our new tool

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // OLD: if (!user.getPassword().equals(password))
        // NEW: Check if raw password matches the Hash in DB
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid Credentials");
        }

        return jwtUtil.generateToken(email);
    }


}
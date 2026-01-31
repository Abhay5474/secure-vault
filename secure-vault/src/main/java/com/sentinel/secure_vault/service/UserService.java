package com.sentinel.secure_vault.service;

import com.sentinel.secure_vault.dto.UserRegistrationDto;
import com.sentinel.secure_vault.model.User;
import com.sentinel.secure_vault.repository.UserRepository;
import com.sentinel.secure_vault.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import java.util.UUID;

@Service // Tells Spring: "This holds business logic"
public class UserService {

    @Autowired // Dependency Injection: Spring automatically gives us the Repository
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder; // Inject the scrambler
    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

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

    // 1. FORGOT PASSWORD (Generate Token & Email)
    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email"));

        // Generate Random Token
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        userRepository.save(user);

        // Send Email
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Secure Vault - Password Reset");
        message.setText("Click the link below to reset your password:\n\n" + resetLink);

        mailSender.send(message);

        return "Reset link sent to your email.";
    }

    // 2. RESET PASSWORD (Verify Token & Update)
    public String resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token) // We need to add this method to Repo
                .orElseThrow(() -> new RuntimeException("Invalid or Expired Token"));

        // Hash the new password (SECURE FORMAT)
        user.setPassword(passwordEncoder.encode(newPassword));

        // Clear the token so it can't be used again
        user.setResetToken(null);
        userRepository.save(user);

        return "Password changed successfully.";
    }



}
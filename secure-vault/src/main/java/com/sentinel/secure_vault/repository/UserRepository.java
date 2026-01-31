package com.sentinel.secure_vault.repository;

import com.sentinel.secure_vault.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// We extend JpaRepository<Type, ID_Type>
// Type = User (The entity we are managing)
// ID_Type = Long (The data type of our Primary Key)
public interface UserRepository extends JpaRepository<User, Long> {

    // 1. Spring generates standard methods (save, findAll, delete) automatically.

    // 2. We define a custom query just by naming the method correctly!
    // "Find a User by their Email"
    // Optional<?> means: "It might return a User, or it might be empty (null safe)"
    Optional<User> findByEmail(String email);
    Optional<User> findByResetToken(String resetToken);
    // Spring sees "findByEmail" -> translates to "SELECT * FROM users WHERE email = ?"
}
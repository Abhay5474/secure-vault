package com.sentinel.secure_vault.model;


import jakarta.persistence.*;
import lombok.Data; // Lombok automatically writes Getters/Setters for us

@Entity // 1. Tells Spring: "This class represents a database table."
@Table(name = "users") // 2. Tells Spring: "Name the table 'users' in MySQL."
@Data // 3. Lombok magic: Generates getters, setters, toString, etc. hiddenly.
public class User {

    @Id // 4. This is the Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 5. Auto-Increment (1, 2, 3...)
    private Long id;

    private String resetToken;
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    @Column(unique = true, nullable = false) // 6. No duplicates allowed, cannot be empty
    private String email;

    @Column(nullable = false) // 7. Password is mandatory
    private String password;

    private String role; // "ADMIN" or "USER"
}

package com.tien.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Users")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive = true;

    @Column(name = "email_verified", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean emailVerified = false;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<UserRole> userRoles;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private Customer customer;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Session> sessions;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Notification> notifications;
    private String otp;

}

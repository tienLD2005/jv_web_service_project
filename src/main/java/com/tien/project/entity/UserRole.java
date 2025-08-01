package com.tien.project.entity;

import jakarta.persistence.*;

import lombok.Data;
import java.time.LocalDateTime;


@Entity
@Table(name = "UserRoles", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "role_id"})
})
@Data
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_role_id")
    private Integer userRoleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "assigned_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime assignedAt;

    public UserRole(User user, Role role) {
        this.user = user;
        this.role = role;
    }

    public UserRole() {

    }
}

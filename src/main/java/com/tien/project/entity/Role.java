package com.tien.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tien.project.entity.enums.ERole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Roles")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private ERole roleName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "role")
    @JsonIgnore
    private List<UserRole> userRoles;
}

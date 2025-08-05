package com.tien.project.repository;

import com.tien.project.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
    Page<UserRole> findByRoleRoleNameContainingIgnoreCase(String roleName, Pageable pageable);
}
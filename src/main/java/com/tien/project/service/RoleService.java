package com.tien.project.service;

import com.tien.project.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoleService {
    List<Role> getAllRoles();
    Role createRole(Role role);
    Role updateRole(Integer id, Role updatedRole);
    void deleteRole(Integer id);
    Page<Role> getRolesWithFilter(String keyword, Pageable pageable);
}
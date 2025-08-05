package com.tien.project.service;

import com.tien.project.entity.Role;

import java.util.List;

public interface RoleService {
    List<Role> getAllRoles();
    Role createRole(Role role);
    Role updateRole(Integer id, Role role);
    void deleteRole(Integer id);
}
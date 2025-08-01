package com.tien.project.service.impl;

import com.tien.project.entity.Role;
import com.tien.project.repository.RoleRepository;
import com.tien.project.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Role createRole(Role role) {
        if (roleRepository.findByRoleName(role.getRoleName()).isPresent()) {
            throw new RuntimeException("Role da ton tai");
        }
        return roleRepository.save(role);
    }

    @Override
    public Role updateRole(Integer id, Role updatedRole) {
        Role existing = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role khong tim thay"));
        existing.setDescription(updatedRole.getDescription());
        return roleRepository.save(existing);
    }

    @Override
    public void deleteRole(Integer id) {
        if (!roleRepository.existsById(id)) {
            throw new RuntimeException("Role khong tim thay");
        }
        roleRepository.deleteById(id);
    }

    @Override
    public Page<Role> getRolesWithFilter(String keyword, Pageable pageable) {
        return roleRepository.findAll(pageable);
    }
}
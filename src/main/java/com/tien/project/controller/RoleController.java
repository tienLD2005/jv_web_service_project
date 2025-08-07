package com.tien.project.controller;

import com.tien.project.dto.response.APIResponse;
import com.tien.project.entity.Role;
import com.tien.project.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;
//================================================QUẢN LÝ ROLE===============================================
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<Role>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        APIResponse.DataWrapper<Role> data = new APIResponse.DataWrapper<>(roles, null);
        return new ResponseEntity<>(new APIResponse<>(true, "Roles retrieved successfully", data, null, null), HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<Role>> createRole(@RequestBody Role role) {
        Role createdRole = roleService.createRole(role);
        APIResponse.DataWrapper<Role> data = new APIResponse.DataWrapper<>(List.of(createdRole), null);
        return new ResponseEntity<>(new APIResponse<>(true, "Role created successfully", data, null, null), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<Role>> updateRole(@PathVariable Integer id, @RequestBody Role role) {
        Role updatedRole = roleService.updateRole(id, role);
        APIResponse.DataWrapper<Role> data = new APIResponse.DataWrapper<>(List.of(updatedRole), null);
        return new ResponseEntity<>(new APIResponse<>(true, "Role updated successfully", data, null, null), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<String>> deleteRole(@PathVariable Integer id) {
        roleService.deleteRole(id);
        APIResponse.DataWrapper<String> data = new APIResponse.DataWrapper<>(List.of("Role deleted"), null);
        return new ResponseEntity<>(new APIResponse<>(true, "Role deleted successfully", data, null, null), HttpStatus.OK);
    }
}
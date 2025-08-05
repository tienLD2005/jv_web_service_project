package com.tien.project.controller;

import com.tien.project.dto.response.APIResponse;
import com.tien.project.entity.UserRole;
import com.tien.project.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-roles")
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<UserRole>> getUserRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String roleName) {
        Page<UserRole> userRolePage = userRoleService.getUserRoles(PageRequest.of(page, size), roleName);
        APIResponse.Pagination pagination = new APIResponse.Pagination(page, size, userRolePage.getTotalPages(), userRolePage.getTotalElements());
        APIResponse.DataWrapper<UserRole> data = new APIResponse.DataWrapper<>(userRolePage.getContent(), pagination);
        return new ResponseEntity<>(new APIResponse<>(true, "User roles retrieved successfully", data, null, null), HttpStatus.OK);
    }
}
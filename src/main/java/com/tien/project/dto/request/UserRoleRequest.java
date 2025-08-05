package com.tien.project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleRequest {
    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotNull(message = "Role ID is required")
    private Integer roleId;
}
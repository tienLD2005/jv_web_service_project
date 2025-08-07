package com.tien.project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleRequest {

    @NotNull(message = "ID người dùng không được để trống")
    private Integer userId;

    @NotNull(message = "ID vai trò không được để trống")
    private Integer roleId;
}

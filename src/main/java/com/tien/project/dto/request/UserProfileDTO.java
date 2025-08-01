package com.tien.project.dto.request;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfileDTO {
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private boolean emailVerified;
}
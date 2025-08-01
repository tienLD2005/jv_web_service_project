package com.tien.project.dto.request;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String phoneNumber;
}
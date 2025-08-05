package com.tien.project.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdate {
    @Size(max = 100, message = "Tên đầy đủ không được vượt quá 100 ký tự")
    private String fullName;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phoneNumber;

//    @Email(message = "Định dạng email không hợp lệ")
//    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
//    private String email;
}
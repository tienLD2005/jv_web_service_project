package com.tien.project.controller;

import com.tien.project.dto.request.UserLogin;
import com.tien.project.dto.request.UserRegister;
import com.tien.project.dto.response.APIResponse;
import com.tien.project.dto.response.ErrorDetail;
import com.tien.project.dto.response.JWTResponse;
import com.tien.project.entity.User;
import com.tien.project.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<APIResponse<User>> registerUser(@Valid @RequestBody UserRegister userRegister, BindingResult result) {
        if (result.hasErrors()) {
            List<ErrorDetail> errors = result.getFieldErrors().stream()
                    .map(error -> new ErrorDetail(error.getField(), error.getDefaultMessage()))
                    .collect(Collectors.toList());
            return new ResponseEntity<>(new APIResponse<>(false, "Dữ liệu không hợp lệ", null, errors), HttpStatus.BAD_REQUEST);
        }

        try {
            User user = userService.registerUser(userRegister);
            APIResponse.DataWrapper<User> data = new APIResponse.DataWrapper<>(List.of(user), null);
            return new ResponseEntity<>(new APIResponse<>(true, "Đăng ký tài khoản thành công", data, null), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new APIResponse<>(false, e.getMessage(), null, null), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/verify-email/{token}")
    public ResponseEntity<APIResponse<String>> verifyEmail(@PathVariable String token) {
        try {
            boolean verified = userService.verifyEmail(token);
            APIResponse.DataWrapper<String> data = new APIResponse.DataWrapper<>(List.of(verified ? "Email đã được xác thực" : "Token không hợp lệ"), null);
            return new ResponseEntity<>(new APIResponse<>(verified, verified ? "Xác thực email thành công" : "Xác thực email thất bại", data, null),
                    verified ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new APIResponse<>(false, e.getMessage(), null, null), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<APIResponse<JWTResponse>> login(@Valid @RequestBody UserLogin userLogin, BindingResult result) {
        if (result.hasErrors()) {
            List<ErrorDetail> errors = result.getFieldErrors().stream()
                    .map(error -> new ErrorDetail(error.getField(), error.getDefaultMessage()))
                    .collect(Collectors.toList());
            return new ResponseEntity<>(new APIResponse<>(false, "Dữ liệu không hợp lệ", null, errors), HttpStatus.BAD_REQUEST);
        }

        try {
            JWTResponse jwtResponse = userService.login(userLogin);
            APIResponse.DataWrapper<JWTResponse> data = new APIResponse.DataWrapper<>(List.of(jwtResponse), null);
            return new ResponseEntity<>(new APIResponse<>(true, "Đăng nhập thành công", data, null), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new APIResponse<>(false, e.getMessage(), null, null), HttpStatus.BAD_REQUEST);
        }
    }
}
package com.tien.project.controller;

import com.tien.project.dto.request.UserLogin;
import com.tien.project.dto.request.UserRegister;
import com.tien.project.dto.response.APIResponse;
import com.tien.project.dto.response.ErrorDetail;
import com.tien.project.dto.response.JWTResponse;
import com.tien.project.entity.User;
import com.tien.project.repository.UserRepository;
import com.tien.project.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<APIResponse<User>> registerUser(@Valid @RequestBody UserRegister userRegister, BindingResult result) {
        if (result.hasErrors()) {
            List<ErrorDetail> errors = result.getFieldErrors().stream()
                    .map(error -> new ErrorDetail(error.getField(), error.getDefaultMessage()))
                    .collect(Collectors.toList());
            return new ResponseEntity<>(new APIResponse<>(false, "Validation failed", null, errors, null), HttpStatus.BAD_REQUEST);
        }
        User user = userService.registerUser(userRegister);
        APIResponse.DataWrapper<User> data = new APIResponse.DataWrapper<>(List.of(user), null);
        return new ResponseEntity<>(new APIResponse<>(true, "Đăng ký tài khoản thành công", data, null, null), HttpStatus.CREATED);
    }

    @PostMapping("/verify-email/{token}")
    public ResponseEntity<APIResponse<String>> verifyEmail(@PathVariable String token) {
        boolean verified = userService.verifyEmail(token);
        APIResponse.DataWrapper<String> data = new APIResponse.DataWrapper<>(List.of(verified ? "Email verified" : "Invalid token"), null);
        return new ResponseEntity<>(new APIResponse<>(verified, verified ? "Xác thực email thành công" : "Xác thực email thất bại", data, null, null),
                verified ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/login")
    public ResponseEntity<APIResponse<JWTResponse>> login(@Valid @RequestBody UserLogin userLogin, BindingResult result) {
        User user = userRepository.findByUsername(userLogin.getUsername()).orElseThrow(()-> new NoSuchElementException("Không tìm thấy tài khoản"));

        if (result.hasErrors()) {
            List<ErrorDetail> errors = result.getFieldErrors().stream()
                    .map(error -> new ErrorDetail(error.getField(), error.getDefaultMessage()))
                    .collect(Collectors.toList());
            return new ResponseEntity<>(new APIResponse<>(false, "Validation failed", null, errors, null), HttpStatus.BAD_REQUEST);
        }
        if (!user.getEmailVerified()){
            return new ResponseEntity<>(new APIResponse<>(false, "Email chưa xác thực", null, null, null), HttpStatus.BAD_REQUEST);
        }
        if (!user.getIsActive()){
            return new ResponseEntity<>(new APIResponse<>(false, "Tài khoản đã bị khoá", null, null, null), HttpStatus.BAD_REQUEST);
        }
        JWTResponse jwtResponse = userService.login(userLogin);
        APIResponse.DataWrapper<JWTResponse> data = new APIResponse.DataWrapper<>(List.of(jwtResponse), null);
        return new ResponseEntity<>(new APIResponse<>(true, "Đăng nhập thành công", data, null, null), HttpStatus.OK);
    }


}
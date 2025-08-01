package com.tien.project.service;

import com.tien.project.dto.request.UserLogin;
import com.tien.project.dto.request.UserRegister;
import com.tien.project.dto.response.JWTResponse;
import com.tien.project.entity.User;

public interface AuthService {
    User registerUser(UserRegister userRegister);
    JWTResponse login(UserLogin userLogin);
    boolean isUsernameTaken(String username);
    boolean isEmailTaken(String email);
}

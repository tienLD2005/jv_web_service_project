package com.tien.project.service.impl;

import com.tien.project.dto.request.UserLogin;
import com.tien.project.dto.request.UserRegister;
import com.tien.project.dto.response.JWTResponse;
import com.tien.project.entity.Role;
import com.tien.project.entity.User;
import com.tien.project.entity.UserRole;
import com.tien.project.entity.enums.ERole;
import com.tien.project.repository.RoleRepository;
import com.tien.project.repository.UserRepository;
import com.tien.project.security.jwt.JWTProvider;
import com.tien.project.security.principal.CustomUserDetails;
import com.tien.project.service.AuthService;
import com.tien.project.service.OtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private JWTProvider jwtProvider;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private OtpService otpService;

    @Override
    public User registerUser(UserRegister userRegister) {
        if (userRepository.existsByUsername(userRegister.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }

        if (userRepository.existsByEmail(userRegister.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        User user = User.builder()
                .username(userRegister.getUsername())
                .passwordHash(passwordEncoder.encode(userRegister.getPassword()))
                .email(userRegister.getEmail())
                .fullName(userRegister.getFullName())
                .phoneNumber(userRegister.getPhoneNumber())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .emailVerified(false)
                .build();

        List<Role> roles = mapRoleStringToRole(userRegister.getRoles());
        List<UserRole> userRoles = roles.stream()
                .map(role -> new UserRole(user, role))
                .collect(Collectors.toList());

        user.setUserRoles(userRoles);
        return userRepository.save(user);
    }

    @Override
    public JWTResponse login(UserLogin userLogin) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userLogin.getUsername(), userLogin.getPassword()));
        } catch (AuthenticationException e) {
            log.error("Sai username hoặc password!");
            throw new RuntimeException("Đăng nhập thất bại!");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtProvider.generateToken(userDetails.getUsername());

        return JWTResponse.builder()
                .username(userDetails.getUsername())
                .fullName(userDetails.getFullName())
                .email(userDetails.getEmail())
                .authorities(userDetails.getAuthorities())
                .token(token)
                .build();
    }

    @Override
    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    private List<Role> mapRoleStringToRole(List<String> roles) {
        List<Role> roleList = new ArrayList<>();

        if (roles != null && !roles.isEmpty()) {
            for (String role : roles) {
                ERole roleEnum = ERole.valueOf(role.toUpperCase());
                Role foundRole = roleRepository.findByRoleName(roleEnum)
                        .orElseThrow(() -> new NoSuchElementException("Không tồn tại role: " + role));
                roleList.add(foundRole);
            }
        } else {
            Role foundRole = roleRepository.findByRoleName(ERole.CUSTOMER)
                    .orElseThrow(() -> new NoSuchElementException("Không tồn tại role: CUSTOMER"));
            roleList.add(foundRole);
        }

        return roleList;
    }
}
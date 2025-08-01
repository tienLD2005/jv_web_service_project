package com.tien.project.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.tien.project.dto.request.*;
import com.tien.project.dto.response.*;
import com.tien.project.entity.RefreshToken;
import com.tien.project.entity.User;
import com.tien.project.repository.UserRepository;
import com.tien.project.security.jwt.JWTProvider;
import com.tien.project.security.principal.CustomUserDetails;
import com.tien.project.service.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;
    private final AuthenticationManager authManager;
    private final UserRepository userRepository;
    private final JWTProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    // ===== 1. ĐĂNG KÝ =====
    @PostMapping("/register")
    public ResponseEntity<APIResponse<User>> registerUser(@RequestBody UserRegister userRegister) {
        User createdUser = authService.registerUser(userRegister);
        otpService.generateAndSendOtp(createdUser); // Gửi OTP ngay sau khi đăng ký
        return ResponseEntity.ok(APIResponse.success(createdUser, "Đăng ký thành công. Vui lòng kiểm tra email để nhận mã OTP."));
    }

    // ===== 2. XÁC MINH OTP =====
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerifyDTO dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        if (!otpService.verifyOtp(user, dto.getOtp())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Mã OTP không đúng hoặc đã hết hạn");
        }

        // Cập nhật emailVerified thành true
        user.setEmailVerified(true);
        userRepository.save(user);

        return ResponseEntity.ok("Xác minh OTP thành công. Bạn có thể đăng nhập.");
    }

    // ===== 3. ĐĂNG NHẬP =====
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLogin userLogin, HttpServletRequest request) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLogin.getUsername(), userLogin.getPassword())
        );

        User user = userRepository.findByUsername(userLogin.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        if (!user.getEmailVerified()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Tài khoản chưa được xác minh email.");
        }

        String accessToken = jwtProvider.generateToken(user.getUsername());
        String ip = request.getRemoteAddr();
        refreshTokenService.manageRefreshTokenLimit(user, ip);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, ip);

        return ResponseEntity.ok(new JWTResponse(accessToken, refreshToken.getToken()));
    }

    // ===== 4. REFRESH TOKEN =====
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String refreshTokenStr = body.get("refreshToken");
        String ip = request.getRemoteAddr();

        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr)
                .orElseThrow(() -> new RuntimeException("Refresh token không hợp lệ"));

        if (!refreshTokenService.isValid(refreshToken, ip)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token không hợp lệ hoặc IP không khớp");
        }

        String accessToken = jwtProvider.generateToken(refreshToken.getUser().getUsername());
        return ResponseEntity.ok(Map.of("accessToken", accessToken));
    }

    // ===== 5. LẤY THÔNG TIN NGƯỜI DÙNG =====
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        return ResponseEntity.ok(new UserProfileDTO(
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getEmailVerified()
        ));
    }

    // ===== 6. CẬP NHẬT THÔNG TIN =====
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(Authentication authentication, @RequestBody UpdateProfileRequest dto) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        user.setFullName(dto.getFullName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok("Cập nhật thông tin thành công");
    }

    // ===== 7. ĐỔI MẬT KHẨU =====
    @PutMapping("/profile/change-password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @RequestBody ChangePasswordRequest dto
    ) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mật khẩu cũ không đúng");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }
}
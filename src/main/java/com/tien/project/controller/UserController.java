package com.tien.project.controller;

import com.tien.project.dto.request.ChangePasswordRequest;
import com.tien.project.dto.request.UserUpdate;
import com.tien.project.dto.response.APIResponse;
import com.tien.project.dto.response.ErrorDetail;
import com.tien.project.entity.User;
import com.tien.project.repository.UserRepository;
import com.tien.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    public ResponseEntity<APIResponse<User>> getProfile(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new APIResponse<>(false, "Không được xác thực", null,
                                Collections.singletonList(new ErrorDetail("Vui lòng cung cấp token hợp lệ")), null));
            }
            User user = userService.getUserByUsername(authentication.getName());
            APIResponse.DataWrapper<User> data = new APIResponse.DataWrapper<>(List.of(user), null);
            return new ResponseEntity<>(new APIResponse<>(true, "Profile retrieved successfully", data, null, null), HttpStatus.OK);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new APIResponse<>(false, "Không có quyền truy cập: " + e.getMessage(), null,
                            Collections.singletonList(new ErrorDetail(e.getMessage())), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new APIResponse<>(false, "Lỗi khi lấy profile: " + e.getMessage(), null,
                            Collections.singletonList(new ErrorDetail(e.getMessage())), null));
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    public ResponseEntity<APIResponse<User>> updateProfile(@RequestBody UserUpdate userUpdate, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new APIResponse<>(false, "Không được xác thực", null,
                                Collections.singletonList(new ErrorDetail("Vui lòng cung cấp token hợp lệ")), null));
            }
            User user = userService.updateProfile(authentication.getName(), userUpdate);
            APIResponse.DataWrapper<User> data = new APIResponse.DataWrapper<>(List.of(user), null);
            return new ResponseEntity<>(new APIResponse<>(true, "Profile updated successfully", data, null, null), HttpStatus.OK);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new APIResponse<>(false, "Không có quyền truy cập: " + e.getMessage(), null,
                            Collections.singletonList(new ErrorDetail(e.getMessage())), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new APIResponse<>(false, "Lỗi khi cập nhật profile: " + e.getMessage(), null,
                            Collections.singletonList(new ErrorDetail(e.getMessage())), null));
        }
    }

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


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<User>> getAllUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<User> userPage = userService.getAllUsers(PageRequest.of(page, size));
        APIResponse.Pagination pagination = new APIResponse.Pagination(page, size, userPage.getTotalPages(), userPage.getTotalElements());
        APIResponse.DataWrapper<User> data = new APIResponse.DataWrapper<>(userPage.getContent(), pagination);
        return new ResponseEntity<>(new APIResponse<>(true, "Users retrieved successfully", data, null, null), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<User>> getUserById(@PathVariable Integer id) {
        User user = userService.getUserById(id);
        APIResponse.DataWrapper<User> data = new APIResponse.DataWrapper<>(List.of(user), null);
        return new ResponseEntity<>(new APIResponse<>(true, "User retrieved successfully", data, null, null), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<User>> updateUser(@PathVariable Integer id, @RequestBody UserUpdate userUpdate) {
        User user = userService.updateUser(id, userUpdate);
        APIResponse.DataWrapper<User> data = new APIResponse.DataWrapper<>(List.of(user), null);
        return new ResponseEntity<>(new APIResponse<>(true, "User updated successfully", data, null, null), HttpStatus.OK);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<String>> updateUserStatus(@PathVariable Integer id, @RequestBody Boolean isActive) {
        userService.updateUserStatus(id, isActive);
        APIResponse.DataWrapper<String> data = new APIResponse.DataWrapper<>(List.of("Status updated"), null);
        return new ResponseEntity<>(new APIResponse<>(true, "User status updated successfully", data, null, null), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<String>> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        APIResponse.DataWrapper<String> data = new APIResponse.DataWrapper<>(List.of("User deleted"), null);
        return new ResponseEntity<>(new APIResponse<>(true, "User deleted successfully", data, null, null), HttpStatus.OK);
    }
}
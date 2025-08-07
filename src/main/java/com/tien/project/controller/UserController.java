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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            User user = userService.getUserByUsername(authentication.getName());
            APIResponse.DataWrapper<User> data = new APIResponse.DataWrapper<>(List.of(user), null);
            return new ResponseEntity<>(new APIResponse<>(true, "Thành công", data, null), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new APIResponse<>(false, "Lỗi", null,
                            List.of(new ErrorDetail("Đã xảy ra lỗi"))));
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    public ResponseEntity<APIResponse<User>> updateProfile(@RequestBody UserUpdate userUpdate, Authentication authentication) {
        try {
            User user = userService.updateProfile(authentication.getName(), userUpdate);
            APIResponse.DataWrapper<User> data = new APIResponse.DataWrapper<>(List.of(user), null);
            return new ResponseEntity<>(new APIResponse<>(true, "Thành công", data, null), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new APIResponse<>(false, "Lỗi", null,
                            List.of(new ErrorDetail("Đã xảy ra lỗi"))));
        }
    }

    @PutMapping("/profile/change-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    public ResponseEntity<APIResponse<String>> changePassword(Authentication authentication,
                                                              @RequestBody ChangePasswordRequest dto) {
        try {
            userService.changePassword(authentication.getName(), dto);
            APIResponse.DataWrapper<String> data = new APIResponse.DataWrapper<>(List.of("OK"), null);
            return ResponseEntity.ok(new APIResponse<>(true, "Đổi mật khẩu thành công", data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new APIResponse<>(false, e.getMessage(), null, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new APIResponse<>(false, "Lỗi", null, List.of(new ErrorDetail("Đã xảy ra lỗi"))));
        }
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<User>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "2") int size) {
        Page<User> userPage = userService.getAllUsers(PageRequest.of(page, size));
        APIResponse.Pagination pagination = new APIResponse.Pagination(page, size, userPage.getTotalPages(), userPage.getTotalElements());
        APIResponse.DataWrapper<User> data = new APIResponse.DataWrapper<>(userPage.getContent(), pagination);
        return new ResponseEntity<>(new APIResponse<>(true, "Thành công", data, null), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<User>> getUserById(@PathVariable Integer id) {
        try {
            User user = userService.getUserById(id);
            APIResponse.DataWrapper<User> data = new APIResponse.DataWrapper<>(List.of(user), null);
            return new ResponseEntity<>(new APIResponse<>(true, "Thành công", data, null), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new APIResponse<>(false, "Không tìm thấy người dùng", null,
                            List.of(new ErrorDetail("ID không hợp lệ"))));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<User>> updateUser(@PathVariable Integer id, @RequestBody UserUpdate userUpdate) {
        try {
            User user = userService.updateUser(id, userUpdate);
            APIResponse.DataWrapper<User> data = new APIResponse.DataWrapper<>(List.of(user), null);
            return new ResponseEntity<>(new APIResponse<>(true, "Cập nhật thành công", data, null), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new APIResponse<>(false, "Lỗi", null,
                            List.of(new ErrorDetail("Đã xảy ra lỗi"))));
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<String>> updateUserStatus(@PathVariable Integer id, @RequestBody Boolean isActive) {
        userService.updateUserStatus(id, isActive);
        APIResponse.DataWrapper<String> data = new APIResponse.DataWrapper<>(List.of("OK"), null);
        return new ResponseEntity<>(new APIResponse<>(true, "Thành công", data, null), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<String>> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        APIResponse.DataWrapper<String> data = new APIResponse.DataWrapper<>(List.of("OK"), null);
        return new ResponseEntity<>(new APIResponse<>(true, "Xóa thành công", data, null), HttpStatus.OK);
    }
}

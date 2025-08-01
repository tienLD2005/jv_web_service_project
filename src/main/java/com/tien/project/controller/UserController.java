package com.tien.project.controller;

import com.tien.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tien.project.entity.User;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        return ResponseEntity.of(userService.getUserById(id));
    }

    @PutMapping("/{id}/update-info")
    public ResponseEntity<User> updateUserInfo(@PathVariable Integer id, @RequestBody User updatedUser) {
        return ResponseEntity.ok(userService.updateUserInfo(id, updatedUser));
    }

    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<Void> toggleUserStatus(@PathVariable Integer id) {
        userService.toggleUserActive(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteUser(@PathVariable Integer id) {
        userService.softDeleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

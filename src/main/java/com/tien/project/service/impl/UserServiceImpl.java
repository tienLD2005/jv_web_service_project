package com.tien.project.service.impl;

import com.tien.project.entity.User;
import com.tien.project.repository.UserRepository;
import com.tien.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    @Override
    public User updateUserInfo(Integer id, User userData) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với id: " + id));

        user.setFullName(userData.getFullName());
        user.setPhoneNumber(userData.getPhoneNumber());
        user.setEmail(userData.getEmail());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    public void toggleUserActive(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với id: " + id));
        user.setIsActive(!user.getIsActive());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void softDeleteUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với id: " + id));
        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}

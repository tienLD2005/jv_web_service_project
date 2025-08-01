package com.tien.project.service;

import com.tien.project.entity.RefreshToken;
import com.tien.project.entity.User;

import java.util.Optional;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user, String ip);
    boolean isValid(RefreshToken token, String ip);
    void deleteByUser(User user);
    Optional<RefreshToken> findByToken(String token);

    void manageRefreshTokenLimit(User user, String ip);
}

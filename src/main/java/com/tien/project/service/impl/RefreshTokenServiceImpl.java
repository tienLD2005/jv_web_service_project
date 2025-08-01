package com.tien.project.service.impl;

import com.tien.project.entity.RefreshToken;
import com.tien.project.entity.User;
import com.tien.project.repository.RefreshTokenRepository;
import com.tien.project.security.jwt.JWTProvider;
import com.tien.project.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private JWTProvider jwtProvider;

    public RefreshToken createRefreshToken(User user, String ip) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setAddressIp(ip);
        token.setExpiryDate(LocalDateTime.now().plusDays(7));
        return refreshTokenRepository.save(token);
    }

    public boolean isValid(RefreshToken token, String ip) {
        return token.getExpiryDate().isAfter(LocalDateTime.now()) && token.getAddressIp().equals(ip);
    }

    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public void manageRefreshTokenLimit(User user, String ip) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUserOrderByExpiryDateAsc(user);
        if (tokens.size() >= 2) {
            refreshTokenRepository.delete(tokens.get(0));
        }
    }

}

package com.tien.project.repository;

import com.tien.project.entity.RefreshToken;
import com.tien.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
    List<RefreshToken> findAllByUserOrderByExpiryDateAsc(User user);
}
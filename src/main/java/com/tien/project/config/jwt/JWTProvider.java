package com.tien.project.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JWTProvider {
    private final String jwtSecret = "8f3m2k9j4h5g6l7p8q0w3e2r5t6y8u0i2o4p6a8s9d0f2g4h6j8k9l0z1x2c3v4b5n6m7";
    private final int jwtExpire = 86400000; // 1 ngày (24 giờ)
    private final int jwtRefresh = 604800000; // 7 ngày
    private final SecretKey secretKey;

    public JWTProvider() {
        log.info("Initializing JWTProvider with jwtSecret: {}", jwtSecret);
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            log.error("JWT secret key is not configured");
            throw new IllegalArgumentException("JWT secret key is not configured");
        }
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        log.info("JWT secret key length: {} bytes", keyBytes.length);
        if (keyBytes.length < 64) {
            log.error("JWT secret key must be at least 64 bytes for HS512, but got {} bytes", keyBytes.length);
            throw new IllegalArgumentException("JWT secret key must be at least 64 bytes for HS512, but got " + keyBytes.length + " bytes");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT secret key initialized successfully with length: {} bytes", keyBytes.length);
    }

    public String generateToken(String username) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + jwtExpire))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("JWT token hết hạn!", e);
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token không được hỗ trợ!", e);
            return false;
        } catch (MalformedJwtException e) {
            log.error("JWT token không đúng định dạng!", e);
            return false;
        } catch (SignatureException e) {
            log.error("Lỗi chữ ký JWT token!", e);
            return false;
        } catch (IllegalArgumentException e) {
            log.error("Lỗi tham số JWT token!", e);
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String refreshToken(String token, String username) {
        if (validateToken(token) && getUsernameFromToken(token).equals(username)) {
            Date now = new Date();
            return Jwts.builder()
                    .setSubject(username)
                    .setIssuedAt(now)
                    .setExpiration(new Date(now.getTime() + jwtRefresh))
                    .signWith(secretKey, SignatureAlgorithm.HS512)
                    .compact();
        }
        return null;
    }

    public int getJwtExpire() {
        return jwtExpire;
    }
}
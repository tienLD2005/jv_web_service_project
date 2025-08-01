package com.tien.project.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JWTProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expire}")
    private long jwtExpire;

    @Value("${jwt.refresh}")
    private long jwtRefresh;
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username){
        Date now = new Date();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + jwtExpire))
                .signWith(getSecretKey(), SignatureAlgorithm.HS512) // ✅ Đã đúng
                .compact();
    }


    public boolean validateToken(String token){
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSecretKey()) // ✅ Đã đúng
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e){
            log.error("JWT token expired!");
        } catch (UnsupportedJwtException e){
            log.error("JWT token unsupported!");
        } catch (MalformedJwtException e){
            log.error("JWT token malformed!");
        } catch (SignatureException e){
            log.error("JWT token signature error!");
        } catch (IllegalArgumentException e){
            log.error("JWT token argument error!");
        }
        return false;
    }


    public String getUsernameFromToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey()) // ✅ Đã đúng
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }


    public String refreshToken(String token, String username){
        if (validateToken(token) && getUsernameFromToken(token).equals(username)) {
            Date now = new Date();
            return Jwts.builder()
                    .setSubject(username)
                    .setIssuedAt(now)
                    .setExpiration(new Date(now.getTime() + jwtRefresh))
                    .signWith(getSecretKey(), SignatureAlgorithm.HS512) // ✅ Đã đúng
                    .compact();
        }
        return null;
    }

    public String extractToken(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }
}
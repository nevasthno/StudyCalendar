package com.example.demo.javaSrc.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${app.jwt.secret}")      private String jwtSecret;
    @Value("${app.jwt.expirationMs}") private long jwtExpirationMs;
    @Value("${app.jwt.issuer}")      private String jwtIssuer;
    
    private Key getSigningKey() {
        // ключ для HS256 должен быть не короче 256 бит
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtExpirationMs);
        return Jwts.builder()
                   .setSubject(username)
                   .setIssuedAt(now)
                   .setExpiration(exp)
                   .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                   .compact();
    }

    // удобная перегрузка
    public String generateToken(Authentication auth) {
        return generateToken(auth.getName());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(getSigningKey())
                   .build()
                   .parseClaimsJws(token)
                   .getBody()
                   .getSubject();
    }
}

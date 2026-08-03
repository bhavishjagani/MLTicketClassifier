package com.SwiftSort.MLTicketClassifier.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${app.jwt.secret:default-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256-algo}")
    private String secret;

    @Value("${app.jwt.access-token-expiration-ms:3600000}")
    private long accessExpiration;

    @Value("${app.jwt.refresh-token-expiration-ms:86400000}")
    private long refreshExpiration;

    public String generateAccessToken(String email, String role) {
        return buildToken(email, role, accessExpiration, "access");
    }

    public String generateRefreshToken(String email) {
        return buildToken(email, null, refreshExpiration, "refresh");
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(parseClaims(token).get("type", String.class));
        } catch (Exception ex) {
            return false;
        }
    }

    private String buildToken(String email, String role, long expiration, String type) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);
        var builder = Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .claim("type", type);
        if (role != null) {
            builder.claim("role", role);
        }
        return builder.signWith(getKey()).compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

package com.SwiftSort.MLTicketClassifier.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256");
        ReflectionTestUtils.setField(jwtService, "accessExpiration", 3600000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 86400000L);
    }

    @Test
    void generateAndValidateAccessToken() {
        String token = jwtService.generateAccessToken("user@test.com", "ADMIN");
        assertTrue(jwtService.isValid(token));
        assertEquals("user@test.com", jwtService.extractEmail(token));
        assertFalse(jwtService.isRefreshToken(token));
    }

    @Test
    void generateAndValidateRefreshToken() {
        String token = jwtService.generateRefreshToken("user@test.com");
        assertTrue(jwtService.isValid(token));
        assertTrue(jwtService.isRefreshToken(token));
    }

    @Test
    void invalidTokenRejected() {
        assertFalse(jwtService.isValid("invalid.token.here"));
    }
}

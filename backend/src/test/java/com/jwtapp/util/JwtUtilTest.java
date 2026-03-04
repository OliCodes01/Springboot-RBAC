package com.jwtapp.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
    }

    @Test
    void testGenerateTokenSuccess() {
        String token = jwtUtil.generateToken(testUserId);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    void testGenerateTokenContainsUserId() {
        String token = jwtUtil.generateToken(testUserId);

        assertNotNull(token);
        UUID extractedId = jwtUtil.extractUserId(token);
        assertEquals(testUserId, extractedId);
    }

    @Test
    void testExtractUserIdSuccess() {
        String token = jwtUtil.generateToken(testUserId);
        UUID extractedId = jwtUtil.extractUserId(token);

        assertEquals(testUserId, extractedId);
    }

    @Test
    void testExtractUserIdInvalidToken() {
        assertThrows(Exception.class, () -> {
            jwtUtil.extractUserId("invalid-token");
        });
    }

    @Test
    void testIsTokenValidWithValidToken() {
        String token = jwtUtil.generateToken(testUserId);
        boolean isValid = jwtUtil.isTokenValid(token);

        assertTrue(isValid);
    }

    @Test
    void testIsTokenValidWithInvalidToken() {
        boolean isValid = jwtUtil.isTokenValid("invalid-token");

        assertFalse(isValid);
    }

    @Test
    void testIsTokenValidWithEmptyToken() {
        boolean isValid = jwtUtil.isTokenValid("");

        assertFalse(isValid);
    }

    @Test
    void testIsTokenValidWithMalformedToken() {
        boolean isValid = jwtUtil.isTokenValid("header.payload.invalid-signature");

        assertFalse(isValid);
    }

    @Test
    void testGenerateMultipleTokensAreDifferent() throws InterruptedException {
        UUID userId2 = UUID.randomUUID();
        String token1 = jwtUtil.generateToken(testUserId);
        Thread.sleep(50); // Delay to ensure different timestamp
        String token2 = jwtUtil.generateToken(userId2);

        assertNotEquals(token1, token2);
    }

    @Test
    void testTokenWithDifferentUserIds() {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        String token1 = jwtUtil.generateToken(userId1);
        String token2 = jwtUtil.generateToken(userId2);

        UUID extractedId1 = jwtUtil.extractUserId(token1);
        UUID extractedId2 = jwtUtil.extractUserId(token2);

        assertEquals(userId1, extractedId1);
        assertEquals(userId2, extractedId2);
        assertNotEquals(extractedId1, extractedId2);
    }
}

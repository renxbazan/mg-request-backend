package com.renx.mg.request.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "testSecretKeyWithAtLeast256BitsForHS256AlgorithmSecurity");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 3600000L);
    }

    @Test
    void generateToken_and_extractUsername_returnsCorrectUsername() {
        String token = jwtUtil.generateToken("testuser", 1L);
        assertThat(token).isNotBlank();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("testuser");
    }

    @Test
    void extractUserId_returnsCorrectId() {
        String token = jwtUtil.generateToken("testuser", 42L);
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(42L);
    }

    @Test
    void validateToken_withValidToken_returnsTrue() {
        String token = jwtUtil.generateToken("testuser", 1L);
        assertThat(jwtUtil.validateToken(token, "testuser")).isTrue();
    }

    @Test
    void validateToken_withWrongUsername_returnsFalse() {
        String token = jwtUtil.generateToken("testuser", 1L);
        assertThat(jwtUtil.validateToken(token, "otheruser")).isFalse();
    }

    @Test
    void validateToken_withInvalidToken_returnsFalse() {
        assertThat(jwtUtil.validateToken("invalid.token.here", "testuser")).isFalse();
    }
}

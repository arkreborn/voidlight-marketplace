package com.voidlight.marketplace.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.voidlight.marketplace.domain.user.Role;
import com.voidlight.marketplace.domain.user.User;

class JwtServiceImplTest {

    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();
        ReflectionTestUtils.setField(jwtService, "secret", "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        ReflectionTestUtils.setField(jwtService, "accessExp", 60L);
        ReflectionTestUtils.setField(jwtService, "refreshExp", 120L);
    }

    @Test
    void generateAndValidateToken() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .roles(Set.of(Role.CUSTOMER))
                .build();
        String token = jwtService.generateAccessToken(user);
        assertTrue(jwtService.isTokenValid(token, user));
        assertEquals("test@example.com", jwtService.extractEmail(token));
    }
}

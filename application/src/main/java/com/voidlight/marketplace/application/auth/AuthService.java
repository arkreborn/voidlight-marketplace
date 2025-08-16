package com.voidlight.marketplace.application.auth;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voidlight.marketplace.application.auth.dto.AuthResponse;
import com.voidlight.marketplace.application.auth.dto.LoginRequest;
import com.voidlight.marketplace.application.auth.dto.RefreshRequest;
import com.voidlight.marketplace.application.auth.dto.RegisterRequest;
import com.voidlight.marketplace.domain.user.Role;
import com.voidlight.marketplace.domain.user.User;
import com.voidlight.marketplace.domain.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(u -> {
            throw new IllegalArgumentException("Email already registered");
        });
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .roles(Set.of(Role.CUSTOMER))
                .createdAt(Instant.now())
                .build();
        userRepository.save(user);
        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);
        return new AuthResponse(access, refresh);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Bad credentials");
        }
        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);
        return new AuthResponse(access, refresh);
    }

    public AuthResponse refresh(RefreshRequest request) {
        String email = jwtService.extractEmail(request.refreshToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));
        if (!jwtService.isTokenValid(request.refreshToken(), user) || !jwtService.isRefreshToken(request.refreshToken())) {
            throw new IllegalArgumentException("Invalid token");
        }
        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);
        return new AuthResponse(access, refresh);
    }
}

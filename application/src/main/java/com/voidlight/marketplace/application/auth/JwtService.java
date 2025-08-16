package com.voidlight.marketplace.application.auth;

import com.voidlight.marketplace.domain.user.User;

public interface JwtService {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    String extractEmail(String token);
    boolean isTokenValid(String token, User user);
    boolean isRefreshToken(String token);
}

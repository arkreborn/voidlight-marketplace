package com.voidlight.marketplace.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.voidlight.marketplace.application.auth.JwtService;
import com.voidlight.marketplace.domain.user.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-exp-minutes:60}")
    private long accessExp;

    @Value("${jwt.refresh-token-exp-minutes:43200}")
    private long refreshExp;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateAccessToken(User user) {
        return buildToken(user, accessExp, "access");
    }

    @Override
    public String generateRefreshToken(User user) {
        return buildToken(user, refreshExp, "refresh");
    }

    private String buildToken(User user, long minutes, String type) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("roles", user.getRoles().stream().map(Enum::name).collect(Collectors.toList()))
                .claim("type", type)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(minutes, ChronoUnit.MINUTES)))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    @Override
    public boolean isTokenValid(String token, User user) {
        Claims claims = parseClaims(token);
        String email = claims.getSubject();
        Date expiration = claims.getExpiration();
        return email.equals(user.getEmail()) && expiration.after(new Date());
    }

    @Override
    public boolean isRefreshToken(String token) {
        return "refresh".equals(parseClaims(token).get("type", String.class));
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

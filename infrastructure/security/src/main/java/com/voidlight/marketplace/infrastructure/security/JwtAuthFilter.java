package com.voidlight.marketplace.infrastructure.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.voidlight.marketplace.application.auth.JwtService;
import com.voidlight.marketplace.domain.user.User;
import com.voidlight.marketplace.domain.user.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            String email = jwtService.extractEmail(token);
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                userRepository.findByEmail(email).ifPresent(user -> setAuthentication(token, user, request));
            }
        }
        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String token, User user, HttpServletRequest request) {
        if (jwtService.isTokenValid(token, user)) {
            var authorities = user.getRoles().stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                    .toList();
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    user.getEmail(), null, authorities);
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }
}

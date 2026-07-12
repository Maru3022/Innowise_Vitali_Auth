package com.example.innowise_vitali.auth.service.impl;

import com.example.innowise_vitali.auth.dto.*;
import com.example.innowise_vitali.auth.entity.*;
import com.example.innowise_vitali.auth.repository.TokenRepository;
import com.example.innowise_vitali.auth.repository.UserRepository;
import com.example.innowise_vitali.auth.service.AuthService;
import com.example.innowise_vitali.auth.service.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername()) || userRepository.existsByEmail(req.getEmail())) {
            throw new BadCredentialsException("Username or email already in use");
        }

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(Role.USER)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        return generateUserTokens(savedUser);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsernameOrEmail(), req.getPassword())
        );

        User user = userRepository.findByUsernameOrEmail(req.getUsernameOrEmail(), req.getUsernameOrEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        revokeAllUserTokens(user);
        return generateUserTokens(user);
    }

    @Override
    @Transactional
    public LoginResponse refresh(RefreshRequest req) {
        String refreshToken = req.getRefreshToken();
        String username = jwtService.extractUsername(refreshToken);
        String tokenType = jwtService.extractTokenType(refreshToken);

        if (username == null || !"refresh".equals(tokenType)) {
            throw new JwtException("Invalid refresh token structure");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new JwtException("User not found"));

        Token storedToken = tokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new JwtException("Token not recognized"));

        if (storedToken.isRevoked() || storedToken.getTokenType() != TokenType.REFRESH || !jwtService.isTokenValid(refreshToken, user)) {
            revokeAllUserTokens(user);
            throw new JwtException("Token expired or compromised. Please login again.");
        }

        revokeAllUserTokens(user);
        return generateUserTokens(user);
    }

    @Override
    @Transactional
    public void logout(String token) {
        if (token != null) {
            tokenRepository.findByToken(token).ifPresent(t -> {
                t.setRevoked(true);
                tokenRepository.save(t);
            });
        }
    }

    @Override
    @Transactional
    public void deleteByUsername(String username) {
        log.warn("Rolling back registration — deleting user with username='{}'", username);
        userRepository.findByUsernameOrEmail(username, username).ifPresent(user -> {
            tokenRepository.deleteAllByUserId(user.getId());
            userRepository.delete(user);
            log.info("Rollback completed for username='{}'", username);
        });
    }

    @Override
    public ValidateTokenResponse validateToken(ValidateTokenRequest req) {
        try {
            String username = jwtService.extractUsername(req.getToken());
            String tokenType = jwtService.extractTokenType(req.getToken());

            if (username != null && "access".equals(tokenType)) {
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null && jwtService.isTokenValid(req.getToken(), user)) {
                    Token storedToken = tokenRepository.findByToken(req.getToken()).orElse(null);
                    if (storedToken != null && !storedToken.isRevoked()) {
                        return ValidateTokenResponse.builder()
                                .valid(true)
                                .userId(user.getId())
                                .role(user.getRole().name())
                                .build();
                    }
                }
            }
            return ValidateTokenResponse.builder().valid(false).build();
        } catch (Exception e) {
            log.warn("Token validation failed via internal call: {}", e.getMessage());
            return ValidateTokenResponse.builder().valid(false).build();
        }
    }

    private LoginResponse generateUserTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(user, accessToken, "access");
        saveUserToken(user, refreshToken, "refresh");

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    private void saveUserToken(User user, String jwtToken, String type) {
        Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType("refresh".equals(type) ? TokenType.REFRESH : TokenType.ACCESS)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        List<Token> validTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if (!validTokens.isEmpty()) {
            validTokens.forEach(t -> t.setRevoked(true));
            tokenRepository.saveAll(validTokens);
        }
    }
}
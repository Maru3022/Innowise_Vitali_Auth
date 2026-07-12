package com.example.innowise_vitali.auth.service;

import com.example.innowise_vitali.auth.dto.*;

public interface AuthService {

    LoginResponse register(RegisterRequest req);

    LoginResponse login(LoginRequest req);

    LoginResponse refresh(RefreshRequest req);

    void logout(String token);

    ValidateTokenResponse validateToken(ValidateTokenRequest req);

    void deleteByUsername(String username);
}
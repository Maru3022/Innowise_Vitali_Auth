package com.example.innowise_vitali.auth.service.impl;

import com.example.innowise_vitali.auth.dto.UserResponse;
import com.example.innowise_vitali.auth.entity.Role;
import com.example.innowise_vitali.auth.entity.User;
import com.example.innowise_vitali.auth.exception.AuthException;
import com.example.innowise_vitali.auth.repository.UserRepository;
import com.example.innowise_vitali.auth.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse setUserStatus(Long userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found with id: " + userId, HttpStatus.NOT_FOUND));

        user.setActive(isActive);
        User updatedUser = userRepository.save(user);
        log.info("Admin updated status for user {}. Active: {}", user.getUsername(), isActive);
        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse setUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found with id: " + userId, HttpStatus.NOT_FOUND));

        try {
            user.setRole(Role.valueOf(roleName.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new AuthException("Invalid role: " + roleName, HttpStatus.BAD_REQUEST);
        }

        User updatedUser = userRepository.save(user);
        log.info("Admin updated role for user {} to {}", user.getUsername(), updatedUser.getRole());
        return mapToResponse(updatedUser);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .active(user.isActive())
                .build();
    }
}
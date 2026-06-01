package com.example.innowise_vitali.auth.service;

import com.example.innowise_vitali.auth.dto.UserResponse;
import java.util.List;

public interface AdminUserService {
    List<UserResponse> getAllUsers();
    UserResponse setUserStatus(Long userId, boolean isActive);
    UserResponse setUserRole(Long userId, String roleName);
}
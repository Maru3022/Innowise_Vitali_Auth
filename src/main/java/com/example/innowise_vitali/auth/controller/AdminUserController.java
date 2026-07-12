package com.example.innowise_vitali.auth.controller;

import com.example.innowise_vitali.auth.dto.UserResponse;
import com.example.innowise_vitali.auth.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<UserResponse> changeUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        return ResponseEntity.ok(adminUserService.setUserStatus(id, active));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> changeUserRole(
            @PathVariable Long id,
            @RequestParam String role) {
        return ResponseEntity.ok(adminUserService.setUserRole(id, role));
    }
}

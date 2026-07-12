package com.example.innowise_vitali.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ValidateTokenResponse {

    private boolean valid;
    private Long userId;
    private String username;
    private String email;
    private String role;
}

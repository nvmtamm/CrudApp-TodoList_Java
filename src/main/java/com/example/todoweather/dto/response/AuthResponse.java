package com.example.todoweather.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String tokenType;
    private String username;
    private String email;
    private String role;
    private String message;

    public static AuthResponse of(String token, String username, String email, String role) {
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .username(username)
                .email(email)
                .role(role)
                .message("Login successful")
                .build();
    }
}

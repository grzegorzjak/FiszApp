package com.example.fiszapp.dto.auth;

public record AuthResponse(
        String message,
        String email,
        boolean emailVerified
) {
}

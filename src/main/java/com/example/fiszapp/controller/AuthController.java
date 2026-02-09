package com.example.fiszapp.controller;

import com.example.fiszapp.dto.auth.AuthResponse;
import com.example.fiszapp.dto.auth.LoginRequest;
import com.example.fiszapp.dto.auth.PasswordResetRequest;
import com.example.fiszapp.dto.auth.RegisterRequest;
import com.example.fiszapp.service.AuthService;
import com.example.fiszapp.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creates a new user account and sends verification email")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify email address", description = "Verifies user email using token from verification link")
    public ResponseEntity<AuthResponse> verifyEmail(@RequestParam String token) {
        AuthResponse response = authService.verifyEmail(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates user and sets JWT token in HttpOnly cookie")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.login(request, response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Clears authentication cookie")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    @PostMapping("/password/reset-request")
    @Operation(summary = "Request password reset", description = "Generates password reset token and sends email (response is always neutral)")
    public ResponseEntity<Map<String, String>> requestPasswordReset(@RequestParam @Email String email) {
        passwordResetService.requestPasswordReset(email);
        return ResponseEntity.ok(Map.of(
                "message",
                "If the email exists, a password reset link has been sent"
        ));
    }

    @PostMapping("/password/reset")
    @Operation(summary = "Reset password", description = "Resets user password using token from reset link")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }
}

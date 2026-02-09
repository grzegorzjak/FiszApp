package com.example.fiszapp.service;

import com.example.fiszapp.dto.auth.AuthResponse;
import com.example.fiszapp.dto.auth.LoginRequest;
import com.example.fiszapp.dto.auth.RegisterRequest;
import com.example.fiszapp.entity.EmailVerificationToken;
import com.example.fiszapp.entity.User;
import com.example.fiszapp.repository.EmailVerificationTokenRepository;
import com.example.fiszapp.repository.UserRepository;
import com.example.fiszapp.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.cookie-name}")
    private String cookieName;

    @Value("${app.jwt.expiration-seconds}")
    private long cookieMaxAge;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole("ROLE_USER");

        user = userRepository.save(user);

        EmailVerificationToken token = createVerificationToken(user);
        verificationTokenRepository.save(token);

        log.info("User registered: {}, verification token: {}", user.getEmail(), token.getToken());

        return new AuthResponse(
                "Registration successful. Please check your email to verify your account.",
                user.getEmail(),
                false
        );
    }

    @Transactional
    public AuthResponse verifyEmail(String token) {
        EmailVerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (!verificationToken.isValid()) {
            throw new IllegalArgumentException("Token is expired or already used");
        }

        User user = verificationToken.getUser();
        user.setEmailVerifiedAt(Instant.now());
        userRepository.save(user);

        verificationToken.setUsedAt(Instant.now());
        verificationTokenRepository.save(verificationToken);

        log.info("Email verified for user: {}", user.getEmail());

        return new AuthResponse(
                "Email verified successfully. You can now log in.",
                user.getEmail(),
                true
        );
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException("Email not verified. Please check your email.");
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String jwt = jwtService.generateToken(user.getEmail());
        setAuthCookie(response, jwt);

        log.info("User logged in: {}", user.getEmail());

        return new AuthResponse(
                "Login successful",
                user.getEmail(),
                true
        );
    }

    public void logout(HttpServletResponse response) {
        clearAuthCookie(response);
    }

    private EmailVerificationToken createVerificationToken(User user) {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(Instant.now().plusSeconds(86400));
        return token;
    }

    private void setAuthCookie(HttpServletResponse response, String jwt) {
        Cookie cookie = new Cookie(cookieName, jwt);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) cookieMaxAge);
        response.addCookie(cookie);
    }

    private void clearAuthCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}

package com.example.fiszapp.service;

import com.example.fiszapp.dto.auth.PasswordResetRequest;
import com.example.fiszapp.entity.PasswordResetToken;
import com.example.fiszapp.entity.User;
import com.example.fiszapp.repository.PasswordResetTokenRepository;
import com.example.fiszapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmailAndDeletedAtIsNull(email)
                .ifPresent(user -> {
                    PasswordResetToken token = createResetToken(user);
                    resetTokenRepository.save(token);

                    log.info("Password reset requested for user: {}, token: {}", email, token.getToken());
                });
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Invalid reset token"));

        if (!resetToken.isValid()) {
            throw new IllegalArgumentException("Token is expired or already used");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetToken.setUsedAt(Instant.now());
        resetTokenRepository.save(resetToken);

        log.info("Password reset successful for user: {}", user.getEmail());
    }

    private PasswordResetToken createResetToken(User user) {
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        return token;
    }
}

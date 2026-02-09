package com.example.fiszapp.security;

import com.example.fiszapp.entity.User;
import com.example.fiszapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public Optional<UserDetails> getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return Optional.of((UserDetails) principal);
        }

        return Optional.empty();
    }

    public Optional<User> getCurrentUser() {
        return getCurrentUserDetails()
                .flatMap(userDetails -> {
                    String email = userDetails.getUsername();
                    return userRepository.findByEmailAndDeletedAtIsNull(email);
                });
    }

    public User getCurrentUserOrThrow() {
        return getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("No authenticated user found"));
    }

    public String getCurrentUserEmail() {
        return getCurrentUserDetails()
                .map(UserDetails::getUsername)
                .orElseThrow(() -> new IllegalStateException("No authenticated user found"));
    }
}

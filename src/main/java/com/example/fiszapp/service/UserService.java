package com.example.fiszapp.service;

import com.example.fiszapp.dto.user.CreateUserRequest;
import com.example.fiszapp.dto.user.UserResponse;
import com.example.fiszapp.entity.User;
import com.example.fiszapp.exception.UserNotFoundException;
import com.example.fiszapp.mapper.UserMapper;
import com.example.fiszapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final WordRepository wordRepository;
    private final CardRepository cardRepository;
    private final CardWordRepository cardWordRepository;
    private final SrsStateRepository srsStateRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        
        return userMapper.toResponse(user);
    }

    @Transactional
    public void deleteAccount(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        
        log.info("Starting account deletion for user {}", userId);
        
        srsStateRepository.deleteByUserId(userId);
        log.debug("Deleted SRS states for user {}", userId);
        
        cardWordRepository.deleteByUserId(userId);
        log.debug("Deleted card-word associations for user {}", userId);
        
        cardRepository.deleteByUserId(userId);
        log.debug("Deleted cards for user {}", userId);
        
        wordRepository.deleteByUserId(userId);
        log.debug("Deleted words for user {}", userId);
        
        userRepository.delete(user);
        log.info("Completed account deletion for user {}", userId);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("User with email " + request.email() + " already exists");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(hashPassword(request.password()));
        
        user = userRepository.save(user);
        log.info("Created user {} with email {}", user.getId(), user.getEmail());
        
        return userMapper.toResponse(user);
    }

    private String hashPassword(String password) {
        return "HASHED_" + password;
    }
}

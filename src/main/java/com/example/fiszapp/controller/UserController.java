package com.example.fiszapp.controller;

import com.example.fiszapp.dto.user.CreateUserRequest;
import com.example.fiszapp.dto.user.UserResponse;
import com.example.fiszapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserResponse> getCurrentUser(
        @RequestAttribute("userId") UUID userId
    ) {
        UserResponse response = userService.getCurrentUser(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(
        @RequestAttribute("userId") UUID userId
    ) {
        userService.deleteAccount(userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /**only for dev purposes */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

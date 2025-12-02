package com.example.fiszapp.controller;

import com.example.fiszapp.dto.generation.GenerationRequest;
import com.example.fiszapp.dto.generation.GenerationResponse;
import com.example.fiszapp.service.GenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/generation-batches")
@RequiredArgsConstructor
public class GenerationController {

    private final GenerationService generationService;
    private static final UUID TEMP_USER_ID = UUID.fromString("ed848abe-5161-4558-b9ad-1a1742cffbbb");

    @PostMapping
    public ResponseEntity<GenerationResponse> generateCards(
        @Valid @RequestBody GenerationRequest request
    ) {
        GenerationResponse response = generationService.generateCards(TEMP_USER_ID, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

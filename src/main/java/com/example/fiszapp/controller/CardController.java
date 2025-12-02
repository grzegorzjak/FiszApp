package com.example.fiszapp.controller;

import com.example.fiszapp.dto.card.CardDetailResponse;
import com.example.fiszapp.dto.card.UpdateCardRequest;
import com.example.fiszapp.dto.common.PageResponse;
import com.example.fiszapp.dto.card.CardResponse;
import com.example.fiszapp.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private static final UUID TEMP_USER_ID = UUID.fromString("ed848abe-5161-4558-b9ad-1a1742cffbbb");

    @GetMapping
    public ResponseEntity<PageResponse<CardResponse>> listCards(
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String sort
    ) {
        PageResponse<CardResponse> response = cardService.listCards(
            TEMP_USER_ID, status, page, size, sort
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDetailResponse> getCard(
        @PathVariable UUID id
    ) {
        CardDetailResponse response = cardService.getCard(TEMP_USER_ID, id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CardDetailResponse> updateCard(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateCardRequest request
    ) {
        CardDetailResponse response = cardService.updateCard(TEMP_USER_ID, id, request);
        return ResponseEntity.ok(response);
    }
}

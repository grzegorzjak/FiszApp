package com.example.fiszapp.controller;

import com.example.fiszapp.dto.common.PageResponse;
import com.example.fiszapp.dto.word.CreateWordRequest;
import com.example.fiszapp.dto.word.UpdateWordRequest;
import com.example.fiszapp.dto.word.WordResponse;
import com.example.fiszapp.service.WordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

    @GetMapping
    public ResponseEntity<PageResponse<WordResponse>> listWords(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) Boolean used,
        @RequestParam(required = false) String search,
        @RequestHeader("userId") UUID userId
    ) {
        PageResponse<WordResponse> response = wordService.listWords(
            userId, page, size, sort, used, search
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<WordResponse> createWord(
        @Valid @RequestBody CreateWordRequest request,
        @RequestHeader("userId") UUID userId
    ) {
        WordResponse response = wordService.createWord(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WordResponse> updateWord(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateWordRequest request,
        @RequestHeader("userId") UUID userId
    ) {
        WordResponse response = wordService.updateWord(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWord(
        @PathVariable UUID id,
        @RequestHeader("userId") UUID userId
    ) {
        wordService.deleteWord(userId, id);
        return ResponseEntity.noContent().build();
    }
}

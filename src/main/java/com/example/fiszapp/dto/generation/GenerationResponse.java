package com.example.fiszapp.dto.generation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerationResponse {
    private UUID batchId;
    private Instant createdAt;
    private int requestedCards;
    private int createdCards;
    private List<GeneratedCardSummary> cards;
}

package com.example.fiszapp.dto.card;

import com.example.fiszapp.dto.enums.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardDetailResponse {
    private UUID id;
    private CardStatus status;
    private String frontEn;
    private String backPl;
    private List<WordSummary> usedWords;
    private Instant createdAt;
    private Instant acceptedAt;
    private Instant archivedAt;
}

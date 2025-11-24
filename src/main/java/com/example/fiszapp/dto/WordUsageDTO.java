package com.example.fiszapp.dto;

import java.util.UUID;

public record WordUsageDTO(
    UUID wordId,
    UUID userId,
    boolean isUsed,
    UUID cardId  // null if word is not used
) {
}

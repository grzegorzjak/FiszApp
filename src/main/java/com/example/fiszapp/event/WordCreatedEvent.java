package com.example.fiszapp.event;

import java.util.UUID;

public record WordCreatedEvent(
    UUID wordId,
    UUID userId,
    String originalText,
    String canonicalText,
    String language
) {
}

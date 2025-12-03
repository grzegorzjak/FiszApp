package com.example.fiszapp.dto.openrouter;

import java.util.List;

public record GeneratedCard(
    String sentenceEn,
    String translationPl,
    List<String> usedWords
) {}

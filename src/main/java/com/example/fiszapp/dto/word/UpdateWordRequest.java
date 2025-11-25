package com.example.fiszapp.dto.word;

import com.example.fiszapp.dto.enums.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateWordRequest(
    @NotBlank(message = "Original text is required")
    @Size(min = 1, max = 200, message = "Original text must be between 1 and 200 characters")
    String originalText,

    @NotNull(message = "Language is required")
    Language language
) {}

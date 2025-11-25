package com.example.fiszapp.dto.word;

import com.example.fiszapp.dto.enums.Language;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordResponse {
    private UUID id;
    private String originalText;
    private String canonicalText;
    private Language language;
    private boolean used;
    private Instant createdAt;
}

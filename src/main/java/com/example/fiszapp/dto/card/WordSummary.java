package com.example.fiszapp.dto.card;

import com.example.fiszapp.dto.enums.Language;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordSummary {
    private UUID id;
    private String originalText;
    private Language language;
}

package com.example.fiszapp.dto.generation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedCardSummary {
    private UUID id;
    private String frontEn;
    private String backPl;
    private List<UUID> usedWordIds;
}

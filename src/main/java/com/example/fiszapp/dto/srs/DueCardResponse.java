package com.example.fiszapp.dto.srs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DueCardResponse {
    private UUID cardId;
    private String frontEn;
    private String backPl;
    private Instant dueAt;
    private Integer intervalDays;
    private Integer repetitions;
    private Short lastGrade;
}

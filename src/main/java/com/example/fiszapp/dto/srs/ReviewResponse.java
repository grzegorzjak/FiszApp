package com.example.fiszapp.dto.srs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private UUID cardId;
    private Instant nextDueAt;
    private Integer intervalDays;
    private Integer repetitions;
}

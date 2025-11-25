package com.example.fiszapp.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatsResponse {
    private LocalDate date;
    private int cardsGenerated;
    private int cardsAccepted;
    private int cardsRejected;
    private double acceptanceRate;
    private int promptsUsed;
    private int promptsLimit;
    private int reviewsDone;
    private int reviewsLimit;
}

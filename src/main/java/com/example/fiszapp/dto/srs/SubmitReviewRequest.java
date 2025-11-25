package com.example.fiszapp.dto.srs;

import com.example.fiszapp.dto.enums.SrsGrade;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitReviewRequest {
    @NotNull(message = "Card ID is required")
    private UUID cardId;

    @NotNull(message = "Grade is required")
    private SrsGrade grade;
}

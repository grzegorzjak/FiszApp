package com.example.fiszapp.dto.generation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerationRequest {
    @Min(value = 1, message = "Max cards must be at least 1")
    @Max(value = 10, message = "Max cards cannot exceed 10")
    private Integer maxCards = 10;
}

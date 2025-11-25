package com.example.fiszapp.dto.word;

import com.example.fiszapp.dto.enums.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWordRequest {
    @NotBlank(message = "Original text is required")
    @Size(min = 1, max = 200, message = "Original text must be between 1 and 200 characters")
    private String originalText;

    @NotNull(message = "Language is required")
    private Language language;
}

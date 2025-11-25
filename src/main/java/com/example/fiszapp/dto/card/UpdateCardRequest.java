package com.example.fiszapp.dto.card;

import com.example.fiszapp.dto.enums.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCardRequest {
    private String frontEn;
    private String backPl;
    private CardStatus status;
}

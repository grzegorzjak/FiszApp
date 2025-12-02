package com.example.fiszapp.mapper;

import com.example.fiszapp.dto.card.CardDetailResponse;
import com.example.fiszapp.dto.card.CardResponse;
import com.example.fiszapp.dto.card.WordSummary;
import com.example.fiszapp.dto.enums.CardStatus;
import com.example.fiszapp.dto.enums.Language;
import com.example.fiszapp.entity.Card;
import com.example.fiszapp.entity.Word;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CardMapper {

    public CardResponse toCardResponse(Card card, List<UUID> usedWordIds) {
        return new CardResponse(
            card.getId(),
            CardStatus.valueOf(card.getStatus().toUpperCase()),
            card.getFrontEn(),
            card.getBackPl(),
            usedWordIds,
            card.getCreatedAt(),
            card.getAcceptedAt(),
            card.getArchivedAt()
        );
    }

    public CardDetailResponse toCardDetailResponse(Card card, List<Word> usedWords) {
        List<WordSummary> wordSummaries = usedWords.stream()
            .map(word -> new WordSummary(
                word.getId(),
                word.getOriginalText(),
                Language.valueOf(word.getLanguage().toUpperCase())
            ))
            .toList();

        return new CardDetailResponse(
            card.getId(),
            CardStatus.valueOf(card.getStatus().toUpperCase()),
            card.getFrontEn(),
            card.getBackPl(),
            wordSummaries,
            card.getCreatedAt(),
            card.getAcceptedAt(),
            card.getArchivedAt()
        );
    }
}

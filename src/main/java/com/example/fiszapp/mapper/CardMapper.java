package com.example.fiszapp.mapper;

import com.example.fiszapp.dto.card.CardDetailResponse;
import com.example.fiszapp.dto.card.CardResponse;
import com.example.fiszapp.dto.card.WordSummary;
import com.example.fiszapp.dto.generation.GeneratedCardSummary;
import com.example.fiszapp.entity.Card;
import com.example.fiszapp.entity.Word;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "usedWordIds", ignore = true)
    CardResponse toResponse(Card card);

    @Mapping(target = "usedWords", ignore = true)
    CardDetailResponse toDetailResponse(Card card);

    @Mapping(target = "usedWordIds", ignore = true)
    GeneratedCardSummary toGeneratedCardSummary(Card card);

    WordSummary toWordSummary(Word word);

    List<WordSummary> toWordSummaryList(List<Word> words);
}

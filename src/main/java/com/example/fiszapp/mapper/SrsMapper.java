package com.example.fiszapp.mapper;

import com.example.fiszapp.dto.srs.DueCardResponse;
import com.example.fiszapp.dto.srs.ReviewResponse;
import com.example.fiszapp.entity.Card;
import com.example.fiszapp.entity.SrsState;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SrsMapper {

    @Mapping(source = "card.id", target = "cardId")
    @Mapping(source = "card.frontEn", target = "frontEn")
    @Mapping(source = "card.backPl", target = "backPl")
    @Mapping(source = "srsState.dueAt", target = "dueAt")
    @Mapping(source = "srsState.intervalDays", target = "intervalDays")
    @Mapping(source = "srsState.repetitions", target = "repetitions")
    @Mapping(source = "srsState.lastGrade", target = "lastGrade")
    DueCardResponse toDueCardResponse(Card card, SrsState srsState);

    @Mapping(source = "cardId", target = "cardId")
    @Mapping(source = "dueAt", target = "nextDueAt")
    @Mapping(source = "intervalDays", target = "intervalDays")
    @Mapping(source = "repetitions", target = "repetitions")
    ReviewResponse toReviewResponse(SrsState srsState);
}

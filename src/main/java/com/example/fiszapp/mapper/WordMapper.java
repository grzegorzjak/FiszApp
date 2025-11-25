package com.example.fiszapp.mapper;

import com.example.fiszapp.dto.word.CreateWordRequest;
import com.example.fiszapp.dto.word.UpdateWordRequest;
import com.example.fiszapp.dto.word.WordResponse;
import com.example.fiszapp.entity.Word;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface WordMapper {

    default WordResponse toResponse(Word word, boolean used) {
        WordResponse response = toResponse(word);
        return new WordResponse(
            response.getId(),
            response.getOriginalText(),
            response.getCanonicalText(),
            response.getLanguage(),
            used,
            response.getCreatedAt()
        );
    }

    @Mapping(target = "used", constant = "false")
    WordResponse toResponse(Word word);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "canonicalText", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "language", source = "language")
    Word toEntity(CreateWordRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "canonicalText", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntityFromRequest(UpdateWordRequest request, @MappingTarget Word word);
}

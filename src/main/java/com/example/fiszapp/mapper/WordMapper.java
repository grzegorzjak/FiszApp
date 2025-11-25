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

    @Mapping(target = "used", ignore = true)
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

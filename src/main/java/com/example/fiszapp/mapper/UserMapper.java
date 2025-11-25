package com.example.fiszapp.mapper;

import com.example.fiszapp.dto.user.UserResponse;
import com.example.fiszapp.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "timezone", constant = "Europe/Warsaw")
    UserResponse toResponse(User user);
}

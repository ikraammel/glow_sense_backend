package com.smart_skin.smart_skin_app_backend.mappers;

import com.smart_skin.smart_skin_app_backend.dto.UserResponseDto;
import com.smart_skin.smart_skin_app_backend.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "deletionRequestedAt", source = "deletionRequestedAt")
    UserResponseDto toDto(User user);
}

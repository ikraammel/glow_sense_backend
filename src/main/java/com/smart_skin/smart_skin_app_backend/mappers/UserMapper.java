package com.smart_skin.smart_skin_app_backend.mappers;

import com.smart_skin.smart_skin_app_backend.dto.UserResponseDto;
import com.smart_skin.smart_skin_app_backend.models.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDto toDto(User user);
}

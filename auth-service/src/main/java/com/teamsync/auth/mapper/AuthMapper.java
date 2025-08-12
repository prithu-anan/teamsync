package com.teamsync.auth.mapper;

import com.teamsync.auth.dto.authDTO.AuthResponseDTO;
import com.teamsync.auth.dto.userDTO.UserResponseDTO;
import com.teamsync.auth.entity.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "user", source = "user")
    @Mapping(target = "token", source = "token")
    @Mapping(target = "refreshToken", source = "refreshToken")
    AuthResponseDTO toAuthResponseDTO(UserResponseDTO user, String token, String refreshToken);
    
    UserResponseDTO toCurrentUserResponseDTO(Users user);
}

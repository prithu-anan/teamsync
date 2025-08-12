package com.teamsync.auth.mapper;

import com.teamsync.auth.dto.userDTO.UserCreationDTO;
import com.teamsync.auth.dto.userDTO.UserResponseDTO;
import com.teamsync.auth.entity.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // User Creation mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profilePicture", ignore = true)
    @Mapping(target = "designation", ignore = true)
    @Mapping(target = "birthdate", ignore = true)
    @Mapping(target = "joinDate", ignore = true)
    @Mapping(target = "predictedBurnoutRisk", ignore = true)
    Users toEntity(UserCreationDTO userCreationDTO);

    UserCreationDTO toCreationDTO(Users user);

    // User Response mappings
    UserResponseDTO toResponseDTO(Users user);

    List<UserResponseDTO> toResponseDTOList(List<Users> users);
}

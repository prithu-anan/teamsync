package com.teamsync.usermanagement.mapper;

import com.teamsync.usermanagement.dto.UserCreationDTO;
import com.teamsync.usermanagement.dto.UserResponseDTO;
import com.teamsync.usermanagement.dto.UserUpdateDTO;
import com.teamsync.usermanagement.entity.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profilePicture", ignore = true)
    @Mapping(target = "profilePictureData", ignore = true)
    @Mapping(target = "designation", ignore = true)
    @Mapping(target = "birthdate", ignore = true)
    @Mapping(target = "joinDate", ignore = true)
    @Mapping(target = "predictedBurnoutRisk", ignore = true)
    Users toEntity(UserCreationDTO userCreationDTO);

    UserResponseDTO toResponseDTO(Users user);

    List<UserResponseDTO> toResponseDTOList(List<Users> users);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "profilePicture", ignore = true)
    @Mapping(target = "profilePictureData", ignore = true)
    @Mapping(target = "designation", ignore = true)
    @Mapping(target = "joinDate", ignore = true)
    @Mapping(target = "predictedBurnoutRisk", ignore = true)
    void updateUserFromDTO(UserUpdateDTO userUpdateDTO, @MappingTarget Users user);
}
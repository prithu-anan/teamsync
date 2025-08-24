package com.teamsync.projectmanagementservice.mapper;
import com.teamsync.projectmanagementservice.dto.ProjectDTO;
import com.teamsync.projectmanagementservice.dto.ProjectMemberDTO;
import com.teamsync.projectmanagementservice.entity.ProjectMembers;
import com.teamsync.projectmanagementservice.entity.Projects;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    ProjectMapper INSTANCE = Mappers.getMapper(ProjectMapper.class);

    @Mapping(source = "createdBy", target = "createdBy")
    ProjectDTO toDto(Projects project);

    @Mapping(source = "user", target = "userId")
    @Mapping(source = "role", target = "role", qualifiedByName = "roleToString")
    ProjectMemberDTO toMemberDto(ProjectMembers member);

    @Named("roleToString")
    default String roleToString(ProjectMembers.ProjectRole role) {
        return role != null ? role.name() : null;
    }

    @Named("stringToRole")
    default ProjectMembers.ProjectRole stringToRole(String role) {
        return role != null ? ProjectMembers.ProjectRole.valueOf(role) : null;
    }
}

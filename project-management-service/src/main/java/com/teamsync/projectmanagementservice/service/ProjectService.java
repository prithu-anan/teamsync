package com.teamsync.projectmanagementservice.service;

import com.teamsync.projectmanagementservice.dto.*;
import com.teamsync.projectmanagementservice.entity.ProjectMembers;
import com.teamsync.projectmanagementservice.entity.Projects;
import com.teamsync.projectmanagementservice.exception.NotFoundException;
import com.teamsync.projectmanagementservice.mapper.ProjectMapper;
import com.teamsync.projectmanagementservice.repository.ProjectMemberRepository;
import com.teamsync.projectmanagementservice.repository.ProjectRepository;
import com.teamsync.usermanagement.event.UserDeletedEvent;
import com.teamsync.projectmanagementservice.client.UserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectsRepository;
    @Autowired
    private ProjectMemberRepository projectMembersRepository;
    @Autowired
    private UserClient userClient;
    @Autowired
    private ProjectMapper projectMapper;

    @KafkaListener(topics = "user-deleted")
    @Transactional
    public void handleUserDeletedEvent(UserDeletedEvent userDeletedEvent) {
        Long userId = userDeletedEvent.getUserId();
        log.info("Received UserDeletedEvent for user: {}", userId);

        try {
            // Remove user from all project memberships
            projectMembersRepository.deleteByUserId(userId);
            log.info("Successfully removed user {} from all project memberships", userId);
        } catch (Exception e) {
            log.error("Failed to remove user {} from project memberships: {}", userId, e.getMessage(), e);
            throw e; // Re-throw to trigger retry mechanism if configured
        }
    }

    public List<ProjectDTO> getAllProjects() {
        return projectsRepository.findAll()
                .stream()
                .map(projectMapper::toDto)
                .collect(Collectors.toList());
    }

    public ProjectDTO getProjectById(Long id) {
        Projects project = projectsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project with ID " + id + " not found"));
        ProjectDTO projectDTO = projectMapper.toDto(project);
        return projectDTO;
    }

    @Transactional
    public void createProject(ProjectCreationDTO createProjectDto, String userEmail) {
        // Get creator user
        UserResponseDTO creator = userClient.findByEmail(userEmail);
        System.out.println(creator);
        if (creator == null) {
            throw new NotFoundException("User not found with email: " + userEmail);
        }

        // Create project
        Projects project = Projects.builder()
                .title(createProjectDto.getTitle())
                .description(createProjectDto.getDescription())
                .createdBy(creator.getId())
                .createdAt(ZonedDateTime.now())
                .build();

        Projects savedProject = projectsRepository.save(project);

        // Add initial members
        if (createProjectDto.getInitialMembers() != null) {
            List<ProjectMembers> members = createProjectDto.getInitialMembers()
                    .stream()
                    .map(memberDto -> {
                        // Validate user exists via userClient
                        UserResponseDTO user = userClient.findById(memberDto.getUserId());
                        if (user == null) {
                            throw new NotFoundException("User not found with id: " + memberDto.getUserId());
                        }

                        return ProjectMembers.builder()
                                .project(savedProject)
                                .user(memberDto.getUserId()) // Pass userId (Long) directly
                                .role(ProjectMembers.ProjectRole.valueOf(memberDto.getRole()))
                                .joinedAt(ZonedDateTime.now())
                                .build();
                    })
                    .collect(Collectors.toList());

            projectMembersRepository.saveAll(members);
        }

        // Fetch the complete project with members
        // Projects completeProject = projectsRepository.findById(savedProject.getId())
        // .orElseThrow(() -> new NotFoundException("Project not found after
        // creation"));

        // return projectMapper.toDto(completeProject);
    }

    @Transactional
    public void updateProject(Long id, ProjectUpdateDTO updateProjectDto) {
        Projects project = projectsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found with id: " + id));
        project.setTitle(updateProjectDto.getTitle());
        project.setDescription(updateProjectDto.getDescription());
        if (updateProjectDto.getMembers() != null) {
            projectMembersRepository.deleteByProjectId(id);
            List<ProjectMembers> newMembers = updateProjectDto.getMembers()
                    .stream()
                    .map(memberDto -> {
                        // Validate user exists via userClient
                        if (userClient.findById(memberDto.getUserId()) == null) {
                            throw new NotFoundException("User not found with id: " + memberDto.getUserId());
                        }

                        return ProjectMembers.builder()
                                .project(project)
                                .user(memberDto.getUserId()) // Use userId (Long) directly
                                .role(ProjectMembers.ProjectRole.valueOf(memberDto.getRole()))
                                .joinedAt(
                                        memberDto.getJoinedAt() != null ? memberDto.getJoinedAt() : ZonedDateTime.now())
                                .build();
                    })
                    .collect(Collectors.toList());
            projectMembersRepository.saveAll(newMembers);
        }
        projectsRepository.save(project);
        // return projectMapper.toDto(savedProject);

    }

    @Transactional
    public void deleteProject(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        if (!projectsRepository.existsById(id)) {
            throw new NotFoundException("Project with ID " + id + " not found");
        }
        projectsRepository.deleteById(id);
    }

    // New member-related methods
    public List<ProjectMemberDTO> getProjectMembers(Long projectId) {
        // First check if project exists
        if (!projectsRepository.existsById(projectId)) {
            throw new NotFoundException("Project not found with id: " + projectId);
        }

        List<ProjectMembers> members = projectMembersRepository.findByProjectId(projectId);
        return members.stream()
                .map(projectMapper::toMemberDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addMemberToProject(Long projectId, AddMemberDTO addMemberDTO) {
        // Validate project exists
        Projects project = projectsRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found with id: " + projectId));

        // Validate user exists
        UserResponseDTO user = userClient.findById(addMemberDTO.getUserId());
        if (user == null) {
            throw new NotFoundException("User not found with id: " + addMemberDTO.getUserId());
        }

        // Check if user is already a member
        if (projectMembersRepository.existsByProjectIdAndUser(projectId, addMemberDTO.getUserId())) {
            throw new IllegalArgumentException("User is already a member of this project");
        }

        // Validate role
        ProjectMembers.ProjectRole role;
        try {
            role = ProjectMembers.ProjectRole.valueOf(addMemberDTO.getRole());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + addMemberDTO.getRole() + ". Valid roles are: " +
                    String.join(", ", java.util.Arrays.stream(ProjectMembers.ProjectRole.values())
                            .map(Enum::name)
                            .toArray(String[]::new)));
        }

        // Create new project member
        ProjectMembers newMember = ProjectMembers.builder()
                .project(project)
                .user(user.getId())
                .role(role)
                .joinedAt(ZonedDateTime.now())
                .build();

        projectMembersRepository.save(newMember);
        // return projectMapper.toMemberDto(savedMember);
    }

    @Transactional
    public void updateMemberRole(Long projectId, Long userId, UpdateMemberRoleDTO updateMemberRoleDTO) {
        ProjectMembers member = projectMembersRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Project member not found with projectId: " + projectId + " and userId: " + userId));

        // Validate role
        ProjectMembers.ProjectRole newRole;
        try {
            newRole = ProjectMembers.ProjectRole.valueOf(updateMemberRoleDTO.getRole());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid role: " + updateMemberRoleDTO.getRole() + ". Valid roles are: " +
                            String.join(", ", java.util.Arrays.stream(ProjectMembers.ProjectRole.values())
                                    .map(Enum::name)
                                    .toArray(String[]::new)));
        }

        member.setRole(newRole);
        projectMembersRepository.save(member);
        // return projectMapper.toMemberDto(updatedMember);
    }

    @Transactional
    public void removeMemberFromProject(Long projectId, Long userId) {
        ProjectMembers member = projectMembersRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Project member not found with projectId: " + projectId + " and userId: " + userId));
        ;
        projectMembersRepository.delete(member);
    }

    public List<UserProjectDTO> getUserProjects(Long userId) {
        // Find all project memberships for the user
        List<ProjectMembers> projectMembers = projectMembersRepository.findByUser(userId);

        return projectMembers.stream()
                .map(member -> {
                    Projects project = member.getProject();

                    // Convert project to DTO
                    ProjectDTO projectDTO = projectMapper.toDto(project);

                    // Build UserProjectDTO
                    return UserProjectDTO.builder()
                            .project(projectDTO)
                            .userRole(member.getRole().name())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public boolean hasUserCreatedProjects(Long userId) {
        return projectsRepository.existsByCreatedBy(userId);
    }

    public boolean existsById(Long id) {
        return projectsRepository.existsById(id);
    }
}
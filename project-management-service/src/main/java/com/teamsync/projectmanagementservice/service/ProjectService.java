package com.teamsync.projectmanagementservice.service;

import com.teamsync.projectmanagementservice.dto.*;
import com.teamsync.projectmanagementservice.entity.ProjectMembers;
import com.teamsync.projectmanagementservice.entity.Projects;
import com.teamsync.projectmanagementservice.exception.NotFoundException;
import com.teamsync.projectmanagementservice.exception.UnauthorizedException;
import com.teamsync.projectmanagementservice.mapper.ProjectMapper;
import com.teamsync.projectmanagementservice.repository.ProjectMemberRepository;
import com.teamsync.projectmanagementservice.repository.ProjectRepository;
import com.teamsync.usermanagement.event.UserDeletedEvent;
import com.teamsync.projectmanagementservice.client.UserClient;
import com.teamsync.projectmanagementservice.event.ProjectDeletedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import com.teamsync.projectmanagementservice.response.SuccessResponse;

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
    @Autowired
    private KafkaTemplate<String, ProjectDeletedEvent> kafkaTemplate;

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
        SuccessResponse<UserResponseDTO> creatorResponse = userClient.findByEmail(userEmail);
        UserResponseDTO creator = creatorResponse.getData();
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
                        SuccessResponse<UserResponseDTO> userResponse = userClient.findById(memberDto.getUserId());
                        UserResponseDTO user = userResponse.getData();
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
    public void updateProject(Long id, ProjectUpdateDTO updateProjectDto, String userEmail) {
        Projects project = projectsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found with id: " + id));
        
        // Check if the current user is the project creator
        SuccessResponse<UserResponseDTO> currentUserResponse = userClient.findByEmail(userEmail);
        UserResponseDTO currentUser = currentUserResponse.getData();
        if (currentUser == null) {
            throw new NotFoundException("User not found with email: " + userEmail);
        }
        
        if (!project.getCreatedBy().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the project creator can update the project");
        }
        
        project.setTitle(updateProjectDto.getTitle());
        project.setDescription(updateProjectDto.getDescription());
        if (updateProjectDto.getMembers() != null) {
            projectMembersRepository.deleteByProjectId(id);
            List<ProjectMembers> newMembers = updateProjectDto.getMembers()
                    .stream()
                    .map(memberDto -> {
                        // Validate user exists via userClient
                        SuccessResponse<UserResponseDTO> userResponse = userClient.findById(memberDto.getUserId());
                        UserResponseDTO user = userResponse.getData();
                        if (user == null) {
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
    public void deleteProject(Long id, String userEmail) {
        if (id == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        
        Projects project = projectsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project with ID " + id + " not found"));
        
        // Check if the current user is the project creator
        SuccessResponse<UserResponseDTO> currentUserResponse = userClient.findByEmail(userEmail);
        UserResponseDTO currentUser = currentUserResponse.getData();
        if (currentUser == null) {
            throw new NotFoundException("User not found with email: " + userEmail);
        }
        
        if (!project.getCreatedBy().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the project creator can delete the project");
        }
        
        try {
            // Delete the project (this will cascade delete project members due to FK constraint)
            projectsRepository.deleteById(id);
            log.info("Successfully deleted project with ID: {}", id);
            
            // Publish event to notify other services about project deletion
            ProjectDeletedEvent event = new ProjectDeletedEvent(
                id, 
                project.getTitle(), 
                currentUser.getId(), 
                java.time.ZonedDateTime.now().toString()
            );
            kafkaTemplate.send("project-deleted", event);
            log.info("Published ProjectDeletedEvent for project ID: {}", id);
            
        } catch (Exception e) {
            log.error("Failed to delete project with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete project: " + e.getMessage(), e);
        }
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
    public void addMemberToProject(Long projectId, AddMemberDTO addMemberDTO, String userEmail) {
        // Validate project exists
        Projects project = projectsRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found with id: " + projectId));

        // Check if the current user is the project creator
        SuccessResponse<UserResponseDTO> currentUserResponse = userClient.findByEmail(userEmail);
        UserResponseDTO currentUser = currentUserResponse.getData();
        if (currentUser == null) {
            throw new NotFoundException("User not found with email: " + userEmail);
        }
        
        if (!project.getCreatedBy().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the project creator can add members to the project");
        }

        // Validate user exists
        SuccessResponse<UserResponseDTO> userResponse = userClient.findById(addMemberDTO.getUserId());
        UserResponseDTO user = userResponse.getData();
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
    public void updateMemberRole(Long projectId, Long userId, UpdateMemberRoleDTO updateMemberRoleDTO, String userEmail) {
        ProjectMembers member = projectMembersRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Project member not found with projectId: " + projectId + " and userId: " + userId));

        // Check if the current user is the project creator
        Projects project = member.getProject();
        SuccessResponse<UserResponseDTO> currentUserResponse = userClient.findByEmail(userEmail);
        UserResponseDTO currentUser = currentUserResponse.getData();
        if (currentUser == null) {
            throw new NotFoundException("User not found with email: " + userEmail);
        }
        
        if (!project.getCreatedBy().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the project creator can update member roles");
        }

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
    public void removeMemberFromProject(Long projectId, Long userId, String userEmail) {
        ProjectMembers member = projectMembersRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Project member not found with projectId: " + projectId + " and userId: " + userId));
        
        // Check if the current user is the project creator
        Projects project = member.getProject();
        SuccessResponse<UserResponseDTO> currentUserResponse = userClient.findByEmail(userEmail);
        UserResponseDTO currentUser = currentUserResponse.getData();
        if (currentUser == null) {
            throw new NotFoundException("User not found with email: " + userEmail);
        }
        
        if (!project.getCreatedBy().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the project creator can remove members from the project");
        }
        
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

    public List<UserProjectDTO> getCurrentUserProjects(String userEmail) {
        // Get current user by email
        SuccessResponse<UserResponseDTO> currentUserResponse = userClient.findByEmail(userEmail);
        UserResponseDTO currentUser = currentUserResponse.getData();
        if (currentUser == null) {
            throw new NotFoundException("User not found with email: " + userEmail);
        }
        
        return getUserProjects(currentUser.getId());
    }

    public boolean hasUserCreatedProjects(Long userId) {
        return projectsRepository.existsByCreatedBy(userId);
    }

    public boolean hasCurrentUserCreatedProjects(String userEmail) {
        SuccessResponse<UserResponseDTO> currentUserResponse = userClient.findByEmail(userEmail);
        UserResponseDTO currentUser = currentUserResponse.getData();
        if (currentUser == null) {
            throw new NotFoundException("User not found with email: " + userEmail);
        }
        
        return hasUserCreatedProjects(currentUser.getId());
    }

    public boolean existsById(Long id) {
        return projectsRepository.existsById(id);
    }
}
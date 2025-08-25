package com.teamsync.projectmanagementservice.controller;

import com.teamsync.projectmanagementservice.client.TaskClient;
// import edu.teamsync.teamsync.authorization.ProjectAuthorizationService;
import com.teamsync.projectmanagementservice.dto.*;
// import edu.teamsync.teamsync.dto.taskDTO.TaskResponseDTO;
import com.teamsync.projectmanagementservice.exception.NotFoundException;
import com.teamsync.projectmanagementservice.response.SuccessResponse;
import com.teamsync.projectmanagementservice.service.ProjectService;
// import edu.teamsync.teamsync.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Collections;

@RestController
@RequestMapping({"/projects", "/api/projects"})
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskClient taskClient;

    // @Autowired
    // private ProjectAuthorizationService authorizationService;

    @GetMapping
    public ResponseEntity<SuccessResponse<List<ProjectDTO>>> getAllProjects() {
        List<ProjectDTO> projects = projectService.getAllProjects();
        SuccessResponse<List<ProjectDTO>> response = SuccessResponse.<List<ProjectDTO>>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Projects retrieved successfully")
                .data(projects)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<ProjectDTO>> getProjectById(@PathVariable Long id) {
        ProjectDTO dto = projectService.getProjectById(id);
        SuccessResponse<ProjectDTO> response = SuccessResponse.<ProjectDTO>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Project retrieved successfully")
                .data(dto)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<Void>> createProject(@Valid @RequestBody ProjectCreationDTO dto) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        projectService.createProject(dto, userEmail);
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.CREATED.value())
                .status(HttpStatus.CREATED)
                .message("Project created successfully")
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> updateProject(@PathVariable Long id,
            @Valid @RequestBody ProjectUpdateDTO dto) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        projectService.updateProject(id, dto, userEmail);
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Project updated successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> deleteProject(@PathVariable Long id) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        projectService.deleteProject(id, userEmail);
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .status(HttpStatus.OK)
                .message("Project deleted successfully")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<SuccessResponse<List<ProjectMemberDTO>>> getProjectMembers(@PathVariable Long id) {
        List<ProjectMemberDTO> members = projectService.getProjectMembers(id);
        SuccessResponse<List<ProjectMemberDTO>> response = SuccessResponse.<List<ProjectMemberDTO>>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Project members retrieved successfully")
                .data(members)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<SuccessResponse<Void>> addMemberToProject(
            @PathVariable Long id, @Valid @RequestBody AddMemberDTO dto) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        projectService.addMemberToProject(id, dto, userEmail);
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.CREATED.value())
                .status(HttpStatus.CREATED)
                .message("Member added to project successfully")
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{projectId}/members/{userId}")
    public ResponseEntity<SuccessResponse<Void>> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateMemberRoleDTO dto) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        projectService.updateMemberRole(projectId, userId, dto, userEmail);
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Member role updated successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<SuccessResponse<Void>> removeMemberFromProject(@PathVariable Long projectId,
            @PathVariable Long userId) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        projectService.removeMemberFromProject(projectId, userId, userEmail);
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .status(HttpStatus.OK)
                .message("Member removed from project successfully")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/user/current")
    public ResponseEntity<List<UserProjectDTO>> getCurrentUserProjects() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        List<UserProjectDTO> userProjects = projectService.getCurrentUserProjects(userEmail);
        return ResponseEntity.ok(userProjects);
    }

    @GetMapping("/user/current/created")
    public ResponseEntity<Boolean> hasCurrentUserCreatedProjects() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean hasCreatedProjects = projectService.hasCurrentUserCreatedProjects(userEmail);
        return ResponseEntity.ok(hasCreatedProjects);
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<SuccessResponse<List<TaskResponseDTO>>> getProjectTasks(@PathVariable Long id) {
        SuccessResponse<List<TaskResponseDTO>> taskResponse = taskClient.getTasksByProjectId(id);
        if (taskResponse == null || taskResponse.getData() == null) {
            return ResponseEntity.ok(SuccessResponse.<List<TaskResponseDTO>>builder()
                    .code(HttpStatus.OK.value())
                    .status(HttpStatus.OK)
                    .message("No tasks found for project")
                    .data(Collections.emptyList())
                    .build());
        }
        return ResponseEntity.ok(taskResponse);
    }

    @GetMapping("/{id}/kanban")
    public ResponseEntity<SuccessResponse<List<TaskResponseDTO>>> getProjectKanban(@PathVariable Long id) {
        SuccessResponse<List<TaskResponseDTO>> kanbanResponse = taskClient.getKanbanBoard(id);
        if (kanbanResponse == null || kanbanResponse.getData() == null) {
            return ResponseEntity.ok(SuccessResponse.<List<TaskResponseDTO>>builder()
                    .code(HttpStatus.OK.value())
                    .status(HttpStatus.OK)
                    .message("No kanban data found for project")
                    .data(Collections.emptyList())
                    .build());
        }
        return ResponseEntity.ok(kanbanResponse);
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<SuccessResponse<Boolean>> existsById(@PathVariable("id") Long id) {
        boolean exists = projectService.existsById(id);
        SuccessResponse<Boolean> response = SuccessResponse.<Boolean>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Project existence check completed")
                .data(exists)
                .build();
        return ResponseEntity.ok(response);
    }
}
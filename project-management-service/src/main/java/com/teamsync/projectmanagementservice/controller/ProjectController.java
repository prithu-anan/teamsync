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

@RestController
@RequestMapping("/projects")
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
        // String userEmail =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        String userEmail = "a@example.com";
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
        projectService.updateProject(id, dto);
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Project updated successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    // @PreAuthorize("@projectAuthorizationService.isProjectAdminOrOwner(#id)")
    public ResponseEntity<SuccessResponse<Void>> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
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
    // @PreAuthorize("@projectAuthorizationService.isProjectAdminOrOwner(#id)")
    public ResponseEntity<SuccessResponse<Void>> addMemberToProject(
            @PathVariable Long id, @Valid @RequestBody AddMemberDTO dto) {
        projectService.addMemberToProject(id, dto);
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.CREATED.value())
                .status(HttpStatus.CREATED)
                .message("Member added to project successfully")
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{projectId}/members/{userId}")
    // @PreAuthorize("@projectAuthorizationService.isProjectAdminOrOwner(#projectId)")
    public ResponseEntity<SuccessResponse<Void>> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateMemberRoleDTO dto) {
        projectService.updateMemberRole(projectId, userId, dto);
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Member role updated successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    // @PreAuthorize("@projectAuthorizationService.isProjectAdminOrOwner(#projectId)")
    public ResponseEntity<SuccessResponse<Void>> removeMemberFromProject(@PathVariable Long projectId,
            @PathVariable Long userId) {
        projectService.removeMemberFromProject(projectId, userId);
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .status(HttpStatus.OK)
                .message("Member removed from project successfully")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserProjectDTO>> getUserProjects(@PathVariable Long userId) {
        List<UserProjectDTO> userProjects = projectService.getUserProjects(userId);
        System.out.println("User Projects: " + userProjects);
        return ResponseEntity.ok(userProjects);
    }

    @GetMapping("/user/{userId}/created")
    public ResponseEntity<Boolean> hasUserCreatedProjects(@PathVariable Long userId) {
        boolean hasCreatedProjects = projectService.hasUserCreatedProjects(userId);
        return ResponseEntity.ok(hasCreatedProjects);
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<SuccessResponse<List<TaskResponseDTO>>> getProjectTasks(@PathVariable Long id) {
        List<TaskResponseDTO> tasks = taskClient.getTasksByProjectId(id);
        SuccessResponse<List<TaskResponseDTO>> response = SuccessResponse.<List<TaskResponseDTO>>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Project tasks retrieved successfully")
                .data(tasks)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/kanban")
    public ResponseEntity<SuccessResponse<List<TaskResponseDTO>>> getProjectKanban(@PathVariable Long id) {
        List<TaskResponseDTO> kanbanData = taskClient.getKanbanBoard(id);

        SuccessResponse<List<TaskResponseDTO>> response = SuccessResponse.<List<TaskResponseDTO>>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Kanban board data retrieved successfully")
                .data(kanbanData)
                .build();
        return ResponseEntity.ok(response);
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
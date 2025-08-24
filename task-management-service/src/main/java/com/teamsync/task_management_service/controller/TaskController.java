package com.teamsync.task_management_service.controller;

import com.teamsync.task_management_service.dto.TaskCreationDTO;
import com.teamsync.task_management_service.dto.TaskResponseDTO;
import com.teamsync.task_management_service.dto.TaskStatusHistoryDTO;
import com.teamsync.task_management_service.dto.TaskUpdateDTO;
import com.teamsync.task_management_service.response.SuccessResponse;
import com.teamsync.task_management_service.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService tasksService; // Changed variable name from taskService to tasksService

    @GetMapping("/user/involved")
    public ResponseEntity<SuccessResponse<List<TaskResponseDTO>>> getUserInvolvedTasks() {

        List<TaskResponseDTO> tasks = tasksService.getUserInvolvedTasks();

        SuccessResponse<List<TaskResponseDTO>> response = SuccessResponse.<List<TaskResponseDTO>>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Current user tasks retrieved successfully")
                .data(tasks)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/assigned")
    public ResponseEntity<SuccessResponse<List<TaskResponseDTO>>> getTasksAssignedToUser() {
        List<TaskResponseDTO> tasks = tasksService.getTasksAssignedToUser();

        SuccessResponse<List<TaskResponseDTO>> response = SuccessResponse.<List<TaskResponseDTO>>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Tasks assigned to user retrieved successfully")
                .data(tasks)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<SuccessResponse<TaskResponseDTO>> getTaskById(@PathVariable Long id) {
        TaskResponseDTO task = tasksService.getTaskById(id);

        SuccessResponse<TaskResponseDTO> response = SuccessResponse.<TaskResponseDTO>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Task retrieved successfully")
                .data(task)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<List<TaskResponseDTO>>> getAllTasks() {
        List<TaskResponseDTO> tasks = tasksService.getAllTasks();

        SuccessResponse<List<TaskResponseDTO>> response = SuccessResponse.<List<TaskResponseDTO>>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("All tasks retrieved successfully")
                .data(tasks)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<SuccessResponse<List<TaskResponseDTO>>> getTasksByProjectId(@PathVariable Long projectId) {
        List<TaskResponseDTO> tasks = tasksService.getTasksByProjectId(projectId);

        SuccessResponse<List<TaskResponseDTO>> response = SuccessResponse.<List<TaskResponseDTO>>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Project tasks retrieved successfully")
                .data(tasks)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    // Removed @PreAuthorize annotation - authorization logic is now handled in the
    // service layer through client calls
    public ResponseEntity<SuccessResponse<Void>> createTask(@Valid @RequestBody TaskCreationDTO createDto) {

        tasksService.createTask(createDto);

        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.CREATED.value())
                .status(HttpStatus.CREATED)
                .message("Task created successfully")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> updateTask(@PathVariable Long id,
            @Valid @RequestBody TaskUpdateDTO updateDto) {
        tasksService.updateTask(id, updateDto);

        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Task updated successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    // Removed @PreAuthorize annotation - authorization logic is now handled in the
    // service layer through client calls
    public ResponseEntity<SuccessResponse<Void>> deleteTask(@PathVariable Long id) {

        tasksService.deleteTask(id);

        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .status(HttpStatus.OK)
                .message("Task deleted successfully")
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<SuccessResponse<TaskResponseDTO>> updateTaskStatus(@PathVariable Long id,
            @RequestBody TaskStatusHistoryDTO dto) {

        SuccessResponse<TaskResponseDTO> response = tasksService.updateTaskStatus(id, dto);

        return ResponseEntity.ok(response);
    }

 

    @GetMapping("/project/{id}/kanban")
    public ResponseEntity<SuccessResponse<List<TaskResponseDTO>>> getKanbanBoard(@PathVariable("id") Long id) {
        List<TaskResponseDTO> tasks = tasksService.getKanbanBoard(id);

        SuccessResponse<List<TaskResponseDTO>> response = SuccessResponse.<List<TaskResponseDTO>>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Kanban board tasks retrieved successfully")
                .data(tasks)
                .build();

        return ResponseEntity.ok(response);
    }
}
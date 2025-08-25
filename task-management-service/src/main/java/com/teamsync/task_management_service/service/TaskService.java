package com.teamsync.task_management_service.service;

import com.teamsync.task_management_service.client.ProjectClient;
import com.teamsync.task_management_service.client.UserClient;
import com.teamsync.task_management_service.dto.ProjectDTO;
import com.teamsync.task_management_service.dto.TaskCreationDTO;
import com.teamsync.task_management_service.dto.TaskResponseDTO;
import com.teamsync.task_management_service.dto.TaskStatusHistoryDTO;
import com.teamsync.task_management_service.dto.TaskUpdateDTO;
import com.teamsync.task_management_service.dto.UserResponseDTO;
import com.teamsync.task_management_service.entity.Tasks;
import com.teamsync.task_management_service.entity.TaskStatusHistory;
import com.teamsync.task_management_service.exception.NotFoundException;
import com.teamsync.task_management_service.exception.UnauthorizedException;
import com.teamsync.task_management_service.mapper.TaskMapper;
import com.teamsync.task_management_service.mapper.TaskStatusHistoryMapper;
import com.teamsync.task_management_service.repository.TaskRepository;
import com.teamsync.task_management_service.repository.TaskStatusHistoryRepository;
import com.teamsync.task_management_service.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import com.teamsync.task_management_service.event.ProjectDeletedEvent;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TaskService {

    private final TaskRepository tasksRepository;
    private final TaskStatusHistoryRepository taskStatusHistoryRepository;
    private final TaskMapper taskMapper;
    private final TaskStatusHistoryMapper statusHistoryMapper;
    private final UserClient userClient;
    private final ProjectClient projectClient;

    public TaskResponseDTO getTaskById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }

        Tasks task = tasksRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + id));

        return buildTaskResponseDto(task);
    }

    public List<TaskResponseDTO> getAllTasks() {
        List<Tasks> tasks = tasksRepository.findAll();
        return tasks.stream()
                .map(this::buildTaskResponseDto)
                .collect(Collectors.toList());
    }

    public void createTask(TaskCreationDTO createDto, String userEmail) {
        if (createDto == null) {
            throw new IllegalArgumentException("Task creation data cannot be null");
        }

        // Validate required fields
        if (createDto.getProjectId() == null) {
            throw new IllegalArgumentException("Project ID is required");
        }

        if (createDto.getTitle() == null || createDto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Task title is required");
        }

        Tasks task = taskMapper.toEntity(createDto);
        task.setAssignedAt(ZonedDateTime.now());

        // Set project (now guaranteed to be not null)
        SuccessResponse<ProjectDTO> projectResponse = projectClient.findById(createDto.getProjectId());
        if (projectResponse == null || projectResponse.getData() == null) {
            throw new NotFoundException("Project not found with id: " + createDto.getProjectId());
        }
        ProjectDTO project = projectResponse.getData();
        task.setProject(project.getId());

        // Set assigned to user if provided
        if (createDto.getAssignedTo() != null) {
            SuccessResponse<UserResponseDTO> assignedUserResponse = userClient.findById(createDto.getAssignedTo());
            if (assignedUserResponse == null || assignedUserResponse.getData() == null) {
                throw new NotFoundException("User not found with id: " + createDto.getAssignedTo());
            }
            UserResponseDTO assignedUser = assignedUserResponse.getData();
            task.setAssignedTo(assignedUser.getId());
            // Set the assignedBy to the current user from security context
            SuccessResponse<UserResponseDTO> currentUserResponse = userClient.findByEmail(userEmail);
            if (currentUserResponse == null || currentUserResponse.getData() == null) {
                throw new NotFoundException("Current user not found with email: " + userEmail);
            }
            UserResponseDTO currentUser = currentUserResponse.getData();
            task.setAssignedBy(currentUser.getId());
        }

        Tasks parentTask = null;
        // Set parent task if provided
        if (createDto.getParentTaskId() != null) {
            parentTask = tasksRepository.findById(createDto.getParentTaskId())
                    .orElseThrow(() -> new NotFoundException(
                            "Parent task not found with id: " + createDto.getParentTaskId()));

            // Validate that parent task belongs to the same project
            if (!parentTask.getProject().equals(createDto.getProjectId())) {
                throw new IllegalArgumentException("Parent task must belong to the same project");
            }
            task.setParentTask(parentTask);
        }

        Tasks savedTask = tasksRepository.save(task);

        // Update parent deadlines recursively if this task has a parent and a deadline
        if (parentTask != null && savedTask.getDeadline() != null) {
            updateParentDeadlinesRecursively(parentTask, savedTask.getDeadline());
        }

        // Create initial status history entry
        createStatusHistoryEntry(savedTask, savedTask.getStatus(), "Task created");
    }

    private void updateParentDeadlinesRecursively(Tasks parentTask, ZonedDateTime newChildDeadline) {
        if (parentTask == null || newChildDeadline == null) {
            return;
        }

        boolean deadlineUpdated = false;

        // Update parent deadline if new child deadline is later
        if (parentTask.getDeadline() == null || newChildDeadline.isAfter(parentTask.getDeadline())) {
            parentTask.setDeadline(newChildDeadline);
            deadlineUpdated = true;
        }

        // Save the updated parent task if deadline was changed
        if (deadlineUpdated) {
            tasksRepository.save(parentTask);

            // Recursively update the parent of this parent task
            if (parentTask.getParentTask() != null) {
                updateParentDeadlinesRecursively(parentTask.getParentTask(), newChildDeadline);
            }
        }
    }

    public void updateTask(Long id, TaskUpdateDTO updateDto, String userEmail) {
        if (id == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }

        if (updateDto == null) {
            throw new IllegalArgumentException("Update data cannot be null");
        }

        Tasks existingTask = tasksRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + id));

        // Check if the current user can manage the task
        if (!canManageTask(id, userEmail)) {
            throw new UnauthorizedException("You are not authorized to update this task");
        }

        // Validate title if provided
        if (updateDto.getTitle() != null && updateDto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }

        // Check if status is changing to create history entry
        boolean statusChanged = updateDto.getStatus() != null &&
                !existingTask.getStatus().name().equals(updateDto.getStatus());
        Tasks.TaskStatus newStatus = null;

        if (statusChanged) {
            try {
                newStatus = Tasks.TaskStatus.valueOf(updateDto.getStatus());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid task status: " + updateDto.getStatus());
            }
        }

        // Update entity using MapStruct
        taskMapper.updateEntityFromDto(updateDto, existingTask);

        // Handle relationships manually (MapStruct doesn't handle these automatically)
        handleRelationshipUpdates(updateDto, existingTask);

        Tasks savedTask = tasksRepository.save(existingTask);

        // Create status history entry if status changed
        if (statusChanged && newStatus != null) {
            createStatusHistoryEntry(savedTask, newStatus, "Status updated");
        }

    }

    public void deleteTask(Long id, String userEmail) {
        if (id == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }

        Tasks task = tasksRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + id));

        // Check if the current user can manage the task
        if (!canManageTask(id, userEmail)) {
            throw new UnauthorizedException("You are not authorized to delete this task");
        }

        try {
            tasksRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete task: " + e.getMessage(), e);
        }
    }

    /**
     * Delete all tasks associated with a specific project.
     * This method is called when a project is deleted to maintain data integrity.
     * 
     * @param projectId the ID of the project whose tasks should be deleted
     */
    @Transactional
    public void deleteTasksByProjectId(Long projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        
        try {
            log.info("Deleting all tasks for project ID: {}", projectId);
            int deletedCount = tasksRepository.deleteByProjectId(projectId);
            log.info("Successfully deleted {} tasks for project ID: {}", deletedCount, projectId);
        } catch (Exception e) {
            log.error("Failed to delete tasks for project ID {}: {}", projectId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete tasks for project: " + e.getMessage(), e);
        }
    }

    /**
     * Kafka listener for ProjectDeletedEvent.
     * Automatically deletes all tasks when a project is deleted.
     * 
     * @param event the ProjectDeletedEvent containing project deletion information
     */
    @KafkaListener(
        topics = "project-deleted", 
        groupId = "task-service"
    )
    @Transactional
    public void handleProjectDeletedEvent(ProjectDeletedEvent event) {
        Long projectId = event.getProjectId();
        log.info("Received ProjectDeletedEvent for project ID: {}, title: {}", projectId, event.getProjectTitle());
        
        try {
            deleteTasksByProjectId(projectId);
            log.info("Successfully processed ProjectDeletedEvent for project ID: {}", projectId);
        } catch (Exception e) {
            log.error("Failed to process ProjectDeletedEvent for project ID {}: {}", projectId, e.getMessage(), e);
            // In a production environment, you might want to implement retry logic or dead letter queue
            throw e;
        }
    }

    public List<TaskResponseDTO> getTasksByProjectId(Long projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }

        // Verify project exists
        SuccessResponse<Boolean> projectExistsResponse = projectClient.existsById(projectId);
        if (projectExistsResponse == null || projectExistsResponse.getData() == null || !projectExistsResponse.getData()) {
            throw new NotFoundException("Project not found with id: " + projectId);
        }

        List<Tasks> tasks = tasksRepository.findByProjectIdWithDetails(projectId);
        return tasks.stream()
                .map(this::buildTaskResponseDto)
                .collect(Collectors.toList());
    }

    public List<TaskResponseDTO> getKanbanBoard(Long projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }

        // Verify project exists
        SuccessResponse<Boolean> projectExistsResponse = projectClient.existsById(projectId);
        if (projectExistsResponse == null || projectExistsResponse.getData() == null || !projectExistsResponse.getData()) {
            throw new NotFoundException("Project not found with id: " + projectId);
        }

        List<Tasks> tasks = tasksRepository.findByProjectIdWithDetails(projectId);
        List<TaskResponseDTO> taskResponseDTOs = new ArrayList<>();
        for (Tasks task : tasks) {
            taskResponseDTOs.add(buildTaskResponseDto(task));
        }
        return taskResponseDTOs;
    }

    private void handleRelationshipUpdates(TaskUpdateDTO updateDto, Tasks existingTask) {
        // Update assigned to user if provided
        if (updateDto.getAssignedTo() != null) {
            SuccessResponse<UserResponseDTO> assignedUserResponse = userClient.findById(updateDto.getAssignedTo());
            if (assignedUserResponse == null || assignedUserResponse.getData() == null) {
                throw new NotFoundException("User not found with id: " + updateDto.getAssignedTo());
            }
            UserResponseDTO assignedUser = assignedUserResponse.getData();
            existingTask.setAssignedTo(assignedUser.getId());
        }

        // Update project if provided
        if (updateDto.getProjectId() != null) {
            SuccessResponse<ProjectDTO> projectResponse = projectClient.findById(updateDto.getProjectId());
            if (projectResponse == null || projectResponse.getData() == null) {
                throw new NotFoundException("Project not found with id: " + updateDto.getProjectId());
            }
            ProjectDTO project = projectResponse.getData();
            existingTask.setProject(project.getId());
        }

        // Update parent task if provided
        if (updateDto.getParentTaskId() != null) {
            Tasks parentTask = tasksRepository.findById(updateDto.getParentTaskId())
                    .orElseThrow(() -> new NotFoundException(
                            "Parent task not found with id: " + updateDto.getParentTaskId()));

            // Validate that parent task belongs to the same project
            if (updateDto.getProjectId() != null && !parentTask.getProject().equals(updateDto.getProjectId())) {
                throw new IllegalArgumentException("Parent task must belong to the same project");
            } else if (updateDto.getProjectId() == null
                    && !parentTask.getProject().equals(existingTask.getProject())) {
                throw new IllegalArgumentException("Parent task must belong to the same project");
            }

            // Prevent circular reference
            if (parentTask.getId().equals(existingTask.getId())) {
                throw new IllegalArgumentException("Task cannot be its own parent");
            }

            existingTask.setParentTask(parentTask);
        }
    }

    private void createStatusHistoryEntry(Tasks task, Tasks.TaskStatus status, String comment) {
        // Get the user who made the change - for now use assignedBy, later replace with
        // authenticated user
        Long changedByUser = task.getAssignedBy();

        // If assignedBy is null, try to use assignedTo, or throw an error
        if (changedByUser == null) {
            changedByUser = task.getAssignedTo();
        }

        // If still null, we need a valid user - you might want to create a system user
        // or handle this differently
        if (changedByUser == null) {
            throw new IllegalStateException("Cannot create status history: no valid user found for changedBy");
        }

        try {
            TaskStatusHistory statusHistory = TaskStatusHistory.builder()
                    .task(task)
                    .status(status)
                    .changedBy(changedByUser)
                    .changedAt(ZonedDateTime.now())
                    .comment(comment)
                    .build();
            taskStatusHistoryRepository.save(statusHistory);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create status history entry: " + e.getMessage(), e);
        }
    }

    private TaskResponseDTO buildTaskResponseDto(Tasks task) {

        TaskResponseDTO dto = taskMapper.toDto(task);
        // Set subtasks
        List<Long> subtaskIds = tasksRepository.findSubtasksByParentTaskId(task.getId())
                .stream()
                .map(Tasks::getId)
                .collect(Collectors.toList());
        dto.setSubtasks(subtaskIds);

        // Set status history
        List<TaskStatusHistory> statusHistoryList = taskStatusHistoryRepository
                .findByTaskIdOrderByChangedAtDesc(task.getId());
        dto.setStatusHistory(statusHistoryMapper.toDtoList(statusHistoryList));

        return dto;
    }

    /*
     * checks if all the children of a task has their status as completed or not
     * 
     * @param task the task to check
     * 
     * @param incompleteChildren the list of incomplete children
     * 
     * @return true if all the children of a task has their status as completed,
     * false otherwise
     */
    private boolean hasAllChildrenCompleted(Long parentTaskId) {

        List<Tasks> children = tasksRepository.findSubtasksByParentTaskId(parentTaskId);

        for (Tasks child : children) {
            if (!child.getStatus().equals(Tasks.TaskStatus.completed)) {
                return false;
            }
        }
        return true;
    }

    public SuccessResponse<TaskResponseDTO> updateTaskStatus(Long taskId, TaskStatusHistoryDTO dto, String userEmail) {

        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + taskId));

        if (Tasks.TaskStatus.valueOf(dto.getStatus()).equals(Tasks.TaskStatus.completed)
                && !canManageTask(taskId, userEmail)) {
            throw new UnauthorizedException("You are not authorized to update the status as completed");
        }

        if (Tasks.TaskStatus.valueOf(dto.getStatus()).equals(Tasks.TaskStatus.completed)
                && !hasAllChildrenCompleted(taskId)) {
            throw new IllegalArgumentException(
                    "All the children of the task must be completed before updating the status to completed");
        }

        if (task.getStatus().equals(Tasks.TaskStatus.completed) && !canManageTask(taskId, userEmail)) {
            throw new UnauthorizedException("You are not authorized to revert back a completed task");
        }

        task.setStatus(Tasks.TaskStatus.valueOf(dto.getStatus()));
        tasksRepository.save(task);

        // Get current user from email
        SuccessResponse<UserResponseDTO> currentUserResponse = userClient.findByEmail(userEmail);
        if (currentUserResponse == null || currentUserResponse.getData() == null) {
            throw new NotFoundException("Current user not found with email: " + userEmail);
        }
        UserResponseDTO currentUser = currentUserResponse.getData();

        TaskStatusHistory statusHistory = TaskStatusHistory.builder()
                .task(task)
                .status(task.getStatus())
                .changedBy(currentUser.getId())
                .changedAt(ZonedDateTime.now())
                .comment(dto.getComment())
                .build();

        taskStatusHistoryRepository.save(statusHistory);

        return SuccessResponse.<TaskResponseDTO>builder()
                .message("Task status updated successfully")
                .data(taskMapper.toDto(task))
                .build();
    }

    public List<TaskResponseDTO> getUserInvolvedTasks(String userEmail) {
        SuccessResponse<UserResponseDTO> currentUserResponse = userClient.findByEmail(userEmail);
        if (currentUserResponse == null || currentUserResponse.getData() == null) {
            throw new NotFoundException("Current user not found with email: " + userEmail);
        }
        UserResponseDTO currentUser = currentUserResponse.getData();
        
        List<Tasks> tasks = tasksRepository.findUserInvolvedTasks(currentUser.getId());
        return tasks.stream()
                .map(this::buildTaskResponseDto)
                .collect(Collectors.toList());
    }

    public List<TaskResponseDTO> getTasksAssignedToUser(String userEmail) {
        SuccessResponse<UserResponseDTO> currentUserResponse = userClient.findByEmail(userEmail);
        if (currentUserResponse == null || currentUserResponse.getData() == null) {
            throw new NotFoundException("Current user not found with email: " + userEmail);
        }
        UserResponseDTO currentUser = currentUserResponse.getData();
        
        List<Tasks> tasks = tasksRepository.findTasksAssignedToUser(currentUser.getId());
        return tasks.stream()
                .map(this::buildTaskResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Check if the current user can manage the task (admin or owner of the project)
     * This replicates the authorization logic from the monolithic
     * ProjectAuthorizationService
     */
    private boolean canManageTask(Long taskId, String userEmail) {
        try {
            // Get the task to find its project
            Tasks task = tasksRepository.findById(taskId)
                    .orElseThrow(() -> new NotFoundException("Task not found with id: " + taskId));

            Long projectId = task.getProject(); // This is now a Long in your microservice

            // Get current user from email
            SuccessResponse<UserResponseDTO> currentUserResponse = userClient.findByEmail(userEmail);
            if (currentUserResponse == null || currentUserResponse.getData() == null) {
                return false;
            }
            UserResponseDTO currentUser = currentUserResponse.getData();

            // Get project details
            SuccessResponse<ProjectDTO> projectResponse = projectClient.findById(projectId);
            if (projectResponse == null || projectResponse.getData() == null) {
                throw new NotFoundException("Project not found");
            }
            ProjectDTO project = projectResponse.getData();

            // Check if user is project creator (owner)
            if (project.getCreatedBy().equals(currentUser.getId())) {
                return true;
            }

            // TODO: For complete microservice architecture, you'll need to add a method to
            // check project membership
            // This would require adding an endpoint to ProjectClient like:
            // @GetMapping("/projects/{projectId}/users/{userId}/can-manage")
            // boolean canUserManageProject(@PathVariable Long projectId, @PathVariable Long
            // userId);

            // For now, using project creator check as authorization
            // You should replace this with proper role checking when you implement project
            // membership endpoints
            return project.getCreatedBy().equals(currentUser.getId());

        } catch (Exception e) {
            // Log the error and return false for safety
            // logger.error("Error checking task management permissions for taskId: " +
            // taskId, e);
            return false;
        }
    }

}
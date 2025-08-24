package com.teamsync.task_management_service.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TaskUpdateDTO {

    @Size(max = 100, message = "Title must be at most 100 characters")
    private String title;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    private String status; // Consider using custom validation if it's an enum like "OPEN", "IN_PROGRESS", etc.

    @FutureOrPresent(message = "Deadline must be in the present or future")
    private ZonedDateTime deadline;

    private String priority;

    @Size(max = 50, message = "Time estimate must be at most 50 characters")
    private String timeEstimate;

    @Size(max = 50, message = "AI time estimate must be at most 50 characters")
    private String aiTimeEstimate;

    private String aiPriority;

    @FutureOrPresent(message = "Smart deadline must be in the present or future")
    private ZonedDateTime smartDeadline;

    private Long projectId;

    private Long assignedTo;

    private Long assignedBy;

    private ZonedDateTime assignedAt;

    private Long parentTaskId;

    private LocalDate tentativeStartingDate;

    private List<Long> subtasks;

    private List<String> attachments;

    private List<TaskStatusHistoryDTO> statusHistory;
}
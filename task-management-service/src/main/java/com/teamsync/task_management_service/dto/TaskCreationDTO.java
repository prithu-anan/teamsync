package com.teamsync.task_management_service.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TaskCreationDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be at most 100 characters")
    private String title;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

//    @NotBlank(message = "Status is required")
    private String status;

//    @NotNull(message = "AssignedTo is required")
    private Long assignedTo;

    @Future(message = "Deadline must be in the future")
    private ZonedDateTime deadline;

//    @NotBlank(message = "Priority is required")
    private String priority;

    private Long parentTaskId;

    @NotNull(message = "Project ID is required")
    private Long projectId;
}
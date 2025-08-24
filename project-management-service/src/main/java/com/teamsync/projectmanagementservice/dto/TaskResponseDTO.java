package com.teamsync.projectmanagementservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
public class TaskResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String status;
    private ZonedDateTime deadline;
    private String priority;
    private String timeEstimate;
    private String aiTimeEstimate;
    private String aiPriority;
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
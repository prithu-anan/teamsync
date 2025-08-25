package com.teamsync.projectmanagementservice.client;

import com.teamsync.projectmanagementservice.dto.TaskResponseDTO;
import com.teamsync.projectmanagementservice.response.SuccessResponse;
import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "task-service", url = "${task-service.url}")
public interface TaskClient {

    @GetMapping("/tasks/project/{id}")
    SuccessResponse<List<TaskResponseDTO>> getTasksByProjectId(@PathVariable("id") Long id);

    @GetMapping("/tasks/project/{id}/kanban")
    SuccessResponse<List<TaskResponseDTO>> getKanbanBoard(@PathVariable("id") Long id);


}

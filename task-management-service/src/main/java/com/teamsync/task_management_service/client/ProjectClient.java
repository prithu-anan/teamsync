package com.teamsync.task_management_service.client;

import com.teamsync.task_management_service.dto.ProjectDTO;
import com.teamsync.task_management_service.response.SuccessResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// import java.util.Optional;

@FeignClient(name = "project-service", url = "${project-service.url}")
public interface ProjectClient {

    @GetMapping("/projects/{id}")
    // Optional<ProjectDTO> findById(@PathVariable("id") Long id);
    SuccessResponse<ProjectDTO> findById(@PathVariable("id") Long id);


    @GetMapping("/projects/{id}/exists")
    SuccessResponse<Boolean> existsById(@PathVariable("id") Long id);
}
package com.teamsync.usermanagement.client;

import com.teamsync.usermanagement.dto.UserProjectDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "project-service", url = "${project-service.url}")
public interface ProjectClient {

    @GetMapping("/projects/user/{userId}")
    List<UserProjectDTO> getUserProjectsByUserId(@PathVariable("userId") Long userId);

    @GetMapping("/projects/user/{userId}/created")
    Boolean hasUserCreatedProjects(@PathVariable("userId") Long userId);
}
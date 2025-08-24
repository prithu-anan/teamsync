package com.teamsync.projectmanagementservice.client;

import  com.teamsync.projectmanagementservice.dto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserClient {

    @GetMapping("/users/projects/{id}")
    UserResponseDTO findById(@PathVariable("id") Long id);

    @GetMapping("/users/projects/email/{email}")
    UserResponseDTO findByEmail(@PathVariable("email") String email);
}

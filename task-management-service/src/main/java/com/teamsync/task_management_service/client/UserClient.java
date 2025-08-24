package com.teamsync.task_management_service.client;

import com.teamsync.task_management_service.dto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserClient {

    @GetMapping("/users/{userId}")
    // Optional<UserResponseDTO> findById(@PathVariable("userId") Long userId);
    UserResponseDTO findById(@PathVariable("userId") Long userId);
    
    @GetMapping("/users/current")
    UserResponseDTO getCurrentUser();


}
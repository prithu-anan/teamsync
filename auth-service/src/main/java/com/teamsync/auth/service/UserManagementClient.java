package com.teamsync.auth.service;

import com.teamsync.auth.dto.authDTO.UserUpdateRequestDTO;
import com.teamsync.auth.dto.userDTO.UserCreationDTO;
import com.teamsync.auth.dto.userDTO.UserResponseDTO;
import com.teamsync.auth.dto.userDTO.UserManagementResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementClient {

    private final RestTemplate restTemplate;

    @Value("${user.management.service.url:http://localhost:8082}")
    private String userManagementServiceUrl;

    public UserResponseDTO createUserInUserManagement(UserCreationDTO userCreationDTO) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                String url = userManagementServiceUrl + "/api/users";
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                HttpEntity<UserCreationDTO> request = new HttpEntity<>(userCreationDTO, headers);
                
                log.info("Creating user in user-management-service (attempt {}): {}", retryCount + 1, userCreationDTO.getEmail());
                
                ResponseEntity<UserManagementResponse> response = restTemplate.postForEntity(
                    url, 
                    request, 
                    UserManagementResponse.class
                );
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    UserManagementResponse responseBody = response.getBody();
                    if (responseBody.getData() != null) {
                        log.info("User created successfully in user-management-service: {}", userCreationDTO.getEmail());
                        return responseBody.getData();
                    } else {
                        log.error("User creation response missing data");
                        throw new RuntimeException("User creation response missing data");
                    }
                } else {
                    log.error("Failed to create user in user-management-service. Status: {}", response.getStatusCode());
                    throw new RuntimeException("Failed to create user in user-management-service");
                }
                
            } catch (Exception e) {
                retryCount++;
                log.warn("Attempt {} failed to create user in user-management-service: {}", retryCount, e.getMessage());
                
                if (retryCount >= maxRetries) {
                    log.error("All {} attempts failed to create user in user-management-service: {}", maxRetries, e.getMessage());
                    throw new RuntimeException("Failed to create user in user-management-service after " + maxRetries + " attempts: " + e.getMessage());
                }
                
                // Wait before retrying (exponential backoff)
                try {
                    Thread.sleep(1000 * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting to retry", ie);
                }
            }
        }
        
        throw new RuntimeException("Failed to create user in user-management-service after " + maxRetries + " attempts");
    }

    public void updateUserProfile(UserUpdateRequestDTO userUpdateRequestDTO) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                String url = userManagementServiceUrl + "/api/users/profile";
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                HttpEntity<UserUpdateRequestDTO> request = new HttpEntity<>(userUpdateRequestDTO, headers);
                
                log.info("Updating user profile in user-management-service (attempt {}): {}", retryCount + 1, userUpdateRequestDTO.getEmail());
                
                ResponseEntity<Void> response = restTemplate.exchange(
                    url, 
                    HttpMethod.PUT, 
                    request, 
                    Void.class
                );
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("User profile updated successfully in user-management-service: {}", userUpdateRequestDTO.getEmail());
                    return;
                } else {
                    log.error("Failed to update user profile in user-management-service. Status: {}", response.getStatusCode());
                    throw new RuntimeException("Failed to update user profile in user-management-service");
                }
                
            } catch (Exception e) {
                retryCount++;
                log.warn("Attempt {} failed to update user profile in user-management-service: {}", retryCount, e.getMessage());
                
                if (retryCount >= maxRetries) {
                    log.error("All {} attempts failed to update user profile in user-management-service: {}", maxRetries, e.getMessage());
                    throw new RuntimeException("Failed to update user profile in user-management-service after " + maxRetries + " attempts: " + e.getMessage());
                }
                
                // Wait before retrying (exponential backoff)
                try {
                    Thread.sleep(1000 * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting to retry", ie);
                }
            }
        }
        
        throw new RuntimeException("Failed to update user profile in user-management-service after " + maxRetries + " attempts");
    }
}

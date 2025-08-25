package com.teamsync.auth.service;

import com.teamsync.auth.dto.authDTO.UserUpdateRequestDTO;
import com.teamsync.auth.dto.userDTO.UserCreationDTO;
import com.teamsync.auth.dto.userDTO.UserResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.time.LocalDate;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${user.management.service.url:http://localhost:8082}")
    private String userManagementServiceUrl;

    public UserResponseDTO createUserInUserManagement(UserCreationDTO userCreationDTO) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                String url = userManagementServiceUrl + "/users";
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                HttpEntity<UserCreationDTO> request = new HttpEntity<>(userCreationDTO, headers);
                
                log.info("Creating user in user-management-service (attempt {}): {}", retryCount + 1, userCreationDTO.getEmail());
                
                ResponseEntity<String> response = restTemplate.postForEntity(
                    url, 
                    request, 
                    String.class
                );
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    try {
                        JsonNode responseNode = objectMapper.readTree(response.getBody());
                        
                        if (responseNode.has("data") && responseNode.get("data") != null) {
                            JsonNode userData = responseNode.get("data");
                            log.info("User created successfully in user-management-service: {}", userCreationDTO.getEmail());
                            return mapJsonToUserResponseDTO(userData);
                        } else {
                            log.error("User creation response missing data");
                            throw new RuntimeException("User creation response missing data");
                        }
                    } catch (Exception e) {
                        log.error("Failed to parse user-management-service response: {}", e.getMessage());
                        throw new RuntimeException("Failed to parse user-management-service response", e);
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
                String url = userManagementServiceUrl + "/users/profile";
                
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

    public UserResponseDTO getUserById(Long userId) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                String url = userManagementServiceUrl + "/users/" + userId;
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                log.info("Fetching user from user-management-service (attempt {}): userId {}", retryCount + 1, userId);
                
                ResponseEntity<String> response = restTemplate.getForEntity(
                    url, 
                    String.class
                );
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    try {
                        JsonNode responseNode = objectMapper.readTree(response.getBody());
                        
                        if (responseNode.has("data") && responseNode.get("data") != null) {
                            JsonNode userData = responseNode.get("data");
                            log.info("User retrieved successfully from user-management-service: userId {}", userId);
                            return mapJsonToUserResponseDTO(userData);
                        } else {
                            log.error("User retrieval response missing data");
                            throw new RuntimeException("User retrieval response missing data");
                        }
                    } catch (Exception e) {
                        log.error("Failed to parse user-management-service response: {}", e.getMessage());
                        throw new RuntimeException("Failed to parse user-management-service response", e);
                    }
                } else {
                    log.error("Failed to retrieve user from user-management-service. Status: {}", response.getStatusCode());
                    throw new RuntimeException("Failed to retrieve user from user-management-service");
                }
                
            } catch (Exception e) {
                retryCount++;
                log.warn("Attempt {} failed to retrieve user from user-management-service: {}", retryCount, e.getMessage());
                
                if (retryCount >= maxRetries) {
                    log.error("All {} attempts failed to retrieve user from user-management-service: {}", maxRetries, e.getMessage());
                    throw new RuntimeException("Failed to retrieve user from user-management-service after " + maxRetries + " attempts: " + e.getMessage());
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
        
        throw new RuntimeException("Failed to retrieve user from user-management-service after " + maxRetries + " attempts");
    }

    private UserResponseDTO mapJsonToUserResponseDTO(JsonNode userData) {
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDate joinDate = null;
        LocalDate birthdate = null;
        
        if (userData.has("joinDate") && userData.get("joinDate") != null) {
            String joinDateStr = userData.get("joinDate").asText();
            if (!"null".equals(joinDateStr)) {
                try {
                    joinDate = LocalDate.parse(joinDateStr);
                    createdAt = joinDate.atStartOfDay();
                } catch (Exception e) {
                    log.warn("Failed to parse joinDate: {}, using current time", joinDateStr);
                    createdAt = LocalDateTime.now();
                }
            }
        }
        
        if (userData.has("birthdate") && userData.get("birthdate") != null) {
            String birthdateStr = userData.get("birthdate").asText();
            if (!"null".equals(birthdateStr)) {
                try {
                    birthdate = LocalDate.parse(birthdateStr);
                } catch (Exception e) {
                    log.warn("Failed to parse birthdate: {}", birthdateStr);
                }
            }
        }
        
        return UserResponseDTO.builder()
                .id(userData.has("id") ? userData.get("id").asLong() : null)
                .name(userData.has("name") ? userData.get("name").asText() : null)
                .email(userData.has("email") ? userData.get("email").asText() : null)
                .isActive(true) // Default to true since user-management-service doesn't have this field
                .createdAt(createdAt)
                .lastLoginAt(LocalDateTime.now()) // We don't have this info from user-management-service
                .profilePicture(userData.has("profilePicture") ? userData.get("profilePicture").asText() : null)
                .designation(userData.has("designation") ? userData.get("designation").asText() : null)
                .birthdate(birthdate)
                .joinDate(joinDate)
                .predictedBurnoutRisk(userData.has("predictedBurnoutRisk") ? userData.get("predictedBurnoutRisk").asBoolean() : null)
                .build();
    }
}

package com.teamsync.auth.service;

import com.teamsync.auth.dto.userDTO.UserCreationDTO;
import com.teamsync.auth.dto.userDTO.UserManagementResponse;
import com.teamsync.auth.dto.userDTO.UserResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManagementClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserManagementClient userManagementClient;

    private UserCreationDTO userCreationDTO;
    private UserManagementResponse userManagementResponse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userManagementClient, "userManagementServiceUrl", "http://localhost:8082");
        
        userCreationDTO = UserCreationDTO.builder()
                .name("Test User")
                .email("test@example.com")
                .password("password123")
                .build();

        UserResponseDTO userResponseDTO = UserResponseDTO.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .lastLoginAt(null)
                .build();

        userManagementResponse = UserManagementResponse.builder()
                .code(201)
                .status("CREATED")
                .message("User created successfully")
                .data(userResponseDTO)
                .build();
    }

    @Test
    void createUserInUserManagement_Success() {
        // Given
        ResponseEntity<UserManagementResponse> responseEntity = 
            new ResponseEntity<>(userManagementResponse, HttpStatus.CREATED);
        
        when(restTemplate.postForEntity(
            anyString(), 
            any(HttpEntity.class), 
            eq(UserManagementResponse.class)
        )).thenReturn(responseEntity);

        // When
        UserResponseDTO result = userManagementClient.createUserInUserManagement(userCreationDTO);

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getName());
    }

    @Test
    void createUserInUserManagement_ResponseMissingData_ThrowsException() {
        // Given
        UserManagementResponse responseWithoutData = UserManagementResponse.builder()
                .code(201)
                .status("CREATED")
                .message("User created successfully")
                .data(null)
                .build();
        
        ResponseEntity<UserManagementResponse> responseEntity = 
            new ResponseEntity<>(responseWithoutData, HttpStatus.CREATED);
        
        when(restTemplate.postForEntity(
            anyString(), 
            any(HttpEntity.class), 
            eq(UserManagementResponse.class)
        )).thenReturn(responseEntity);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userManagementClient.createUserInUserManagement(userCreationDTO);
        });
    }

    @Test
    void createUserInUserManagement_NonSuccessStatus_ThrowsException() {
        // Given
        ResponseEntity<UserManagementResponse> responseEntity = 
            new ResponseEntity<>(userManagementResponse, HttpStatus.BAD_REQUEST);
        
        when(restTemplate.postForEntity(
            anyString(), 
            any(HttpEntity.class), 
            eq(UserManagementResponse.class)
        )).thenReturn(responseEntity);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userManagementClient.createUserInUserManagement(userCreationDTO);
        });
    }
}

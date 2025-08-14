package com.teamsync.auth.dto.userDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    
    // Additional fields from user-management-service
    private String profilePicture;
    private String designation;
    private LocalDate birthdate;
    private LocalDate joinDate;
    private Boolean predictedBurnoutRisk;
}

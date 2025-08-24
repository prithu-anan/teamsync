package com.teamsync.projectmanagementservice.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProjectCreationDTO {
    @Size(max = 100, message = "Title must be at most 100 characters")
    @NotNull(message = "Title is required")
    private String title;
    
    @Size(max = 1000, message = "Description must be at most 1000 characters")
    @NotNull(message = "Description is required")
    private String description;
    
    private List<InitialMemberDTO> initialMembers;
}
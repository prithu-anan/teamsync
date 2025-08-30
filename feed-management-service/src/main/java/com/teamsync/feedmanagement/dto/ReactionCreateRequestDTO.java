package com.teamsync.feedmanagement.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReactionCreateRequestDTO {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Reaction type is required")
    private String reactionType;
}
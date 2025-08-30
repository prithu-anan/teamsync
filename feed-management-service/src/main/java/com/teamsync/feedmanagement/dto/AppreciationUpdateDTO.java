package com.teamsync.feedmanagement.dto;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.time.ZonedDateTime;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AppreciationUpdateDTO {
    @NotNull(message = "From user ID is required")
    private Long fromUserId;

    @NotNull(message = "To user ID is required")
    private Long toUserId;

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "Timestamp is required")
    private ZonedDateTime timestamp;
}
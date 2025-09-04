package com.teamsync.message_management_service.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


// MessageUpdateDTO - Add isPinned field (optional)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record MessageUpdateDTO(
        @NotNull(message = "Channel id cannot be null")
        Long channelId,
        @NotNull(message = "Recipient id cannot be null")
        Long recipientId,
        @NotBlank(message = "Content cannot be blank")
        String content,
        Boolean isPinned  // Add this field - optional for updates
) {}

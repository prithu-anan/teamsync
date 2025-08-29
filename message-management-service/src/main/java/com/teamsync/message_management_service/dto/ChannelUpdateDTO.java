package com.teamsync.message_management_service.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.teamsync.message_management_service.entity.Channels.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ChannelUpdateDTO(
        @NotBlank(message = "Name cannot be blank")
        String name,

        @NotNull(message = "Type cannot be null")
        ChannelType type,

        @NotNull(message = "Project ID cannot be null")
        Long projectId,

        @NotNull(message = "Members cannot be null")
        @Size(min = 1, message = "At least one member is required")
        List<Long> members
) {}
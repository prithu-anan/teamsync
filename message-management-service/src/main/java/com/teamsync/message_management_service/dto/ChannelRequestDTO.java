package com.teamsync.message_management_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.teamsync.message_management_service.entity.Channels.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ChannelRequestDTO(
        @NotBlank(message = "Name cannot be blank")
        String name,

        @NotNull(message = "Type cannot be null")
        ChannelType type,

        @NotNull(message = "Project ID cannot be null")
        @JsonProperty("project_id")
        Long projectId,

        @NotNull(message = "Member IDs cannot be null")
        @Size(min = 1, message = "At least one member ID is required")
        @JsonProperty("member_ids")
        List<Long> memberIds
) {}

package com.teamsync.message_management_service.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.teamsync.message_management_service.entity.Channels.ChannelType;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ChannelResponseDTO(
        Long id,
        String name,
        ChannelType type,
        Long projectId,
        List<Long> members
) {}
package com.teamsync.message_management_service.dto;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReactionResponseDTO {
    private Long id;
    private Long userId;
    private String reactionType;
    private ZonedDateTime createdAt;
}

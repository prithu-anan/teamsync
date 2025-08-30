package com.teamsync.feedmanagement.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.teamsync.feedmanagement.entity.Events.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record EventCreationDTO(
                @NotBlank(message = "Title cannot be blank") String title,

                String description,

                @NotNull(message = "Type cannot be null") EventType type,

                @NotNull(message = "Date cannot be null") LocalDate date,

                @NotNull(message = "Participant IDs cannot be null") @Size(min = 1, message = "At least one participant ID is required") List<Long> participantIds) {
}
package com.teamsync.feedmanagement.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PollVoteUpdateDTO  {
    @NotNull(message = "Poll ID is required")
    private Long pollId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Selected option is required")
    private String selectedOption;
}


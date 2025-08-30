package com.teamsync.feedmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyCreateRequestDTO {
    @NotBlank(message = "Reply content cannot be blank")
    @Size(min = 1, max = 1000, message = "Reply content must be between 1 and 1000 characters")
    private String content;

    @NotNull(message = "Author ID is required")
    @Positive(message = "Author ID must be a positive number")
    private Long author_id;
}

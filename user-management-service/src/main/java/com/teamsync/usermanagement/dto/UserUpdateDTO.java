package com.teamsync.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDTO {
    @NotBlank(message = "Name is required")
    private String name;

    private LocalDate birthdate;
}
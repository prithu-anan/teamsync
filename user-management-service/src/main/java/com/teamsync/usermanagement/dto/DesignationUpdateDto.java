package com.teamsync.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.Pattern;

@Builder
@Data
public class DesignationUpdateDto {
    @NotBlank(message = "Designation is required")
    @Pattern(
        regexp = "^(manager|employee)$",
        message = "Designation must be either 'manager' or 'employee'"
    )
    private String designation;
}

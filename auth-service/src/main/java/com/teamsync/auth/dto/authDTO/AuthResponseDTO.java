package com.teamsync.auth.dto.authDTO;

import com.teamsync.auth.dto.userDTO.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {
    private UserResponseDTO user;
    private String token;
    private String refreshToken;
}

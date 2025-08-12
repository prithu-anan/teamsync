package com.teamsync.auth.controller;

import com.teamsync.auth.dto.authDTO.*;
import com.teamsync.auth.dto.userDTO.UserCreationDTO;
import com.teamsync.auth.dto.userDTO.UserResponseDTO;
import com.teamsync.auth.response.SuccessResponse;
import com.teamsync.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuccessResponse<UserResponseDTO>> registerUser(
            @Valid @RequestBody UserCreationDTO userCreationDTO,
            HttpServletRequest request) {

        UserResponseDTO authResponse = authService.registerUser(userCreationDTO, request);

        SuccessResponse<UserResponseDTO> response = SuccessResponse.<UserResponseDTO>builder()
                .code(HttpStatus.CREATED.value())
                .status(HttpStatus.CREATED)
                .message("User registered successfully")
                .data(authResponse)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuccessResponse<AuthResponseDTO>> loginUser(
            @Valid @RequestBody LoginRequestDTO loginRequestDTO,
            HttpServletRequest request) {

        AuthResponseDTO authResponse = authService.loginUser(loginRequestDTO, request);

        SuccessResponse<AuthResponseDTO> response = SuccessResponse.<AuthResponseDTO>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("User logged in successfully")
                .data(authResponse)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuccessResponse<TokenRefreshResponseDTO>> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDTO refreshTokenRequest) {

        TokenRefreshResponseDTO tokenResponse = authService.refreshToken(refreshTokenRequest.getRefreshToken());

        SuccessResponse<TokenRefreshResponseDTO> response = SuccessResponse.<TokenRefreshResponseDTO>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Token refreshed successfully")
                .data(tokenResponse)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuccessResponse<Void>> logout(
            @RequestBody RefreshTokenRequestDTO refreshTokenRequest,
            HttpServletRequest request) {

        String refreshToken = refreshTokenRequest.getRefreshToken();

        // Extract JWT token from Authorization header
        String jwtToken = request.getHeader("Authorization");
        if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
            jwtToken = jwtToken.substring(7);
        }

        // Call logout with both tokens
        authService.logout(refreshToken, jwtToken);

        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("User logged out successfully")
                .data(null)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuccessResponse<UserResponseDTO>> getCurrentUser() {

        UserResponseDTO userResponse = authService.getCurrentUser();

        SuccessResponse<UserResponseDTO> response = SuccessResponse.<UserResponseDTO>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Current user retrieved successfully")
                .data(userResponse)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuccessResponse<Void>> updateCurrentUser(@Valid @RequestBody UserUpdateRequestDTO requestDTO) {

        authService.updateCurrentUser(requestDTO);

        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Current User updated successfully")
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/password-change", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuccessResponse<String>> changePassword(@Valid @RequestBody PasswordChangeRequestDTO requestDTO) {

        authService.changePassword(requestDTO);

        SuccessResponse<String> response = SuccessResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Password changed successfully")
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/password-reset-request", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuccessResponse<Void>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestDTO request) {

        authService.requestPasswordReset(request);

        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("If the email exists, a password reset link has been sent")
                .data(null)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/password-reset", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuccessResponse<Void>> resetPassword(
            @Valid @RequestBody PasswordResetDTO resetRequest) {

        authService.resetPassword(resetRequest);

        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Password reset successfully")
                .data(null)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

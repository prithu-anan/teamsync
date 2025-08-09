package com.teamsync.usermanagement.controller;

import com.teamsync.usermanagement.dto.UserCreationDTO;
import com.teamsync.usermanagement.dto.UserResponseDTO;
import com.teamsync.usermanagement.dto.UserUpdateDTO;
import com.teamsync.usermanagement.response.SuccessResponse;
import com.teamsync.usermanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<SuccessResponse<UserResponseDTO>> createUser(
            @Valid @RequestBody UserCreationDTO userDto) {
        
        UserResponseDTO createdUser = userService.createUser(userDto);

        SuccessResponse<UserResponseDTO> response = SuccessResponse.<UserResponseDTO>builder()
                .code(HttpStatus.CREATED.value())
                .status(HttpStatus.CREATED)
                .message("User created successfully")
                .data(createdUser)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<List<UserResponseDTO>>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();

        SuccessResponse<List<UserResponseDTO>> response = SuccessResponse.<List<UserResponseDTO>>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Users retrieved successfully")
                .data(users)
                .metadata(Map.of("count", users.size()))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<UserResponseDTO>> getUser(@PathVariable Long id) {
        UserResponseDTO user = userService.getUser(id);

        SuccessResponse<UserResponseDTO> response = SuccessResponse.<UserResponseDTO>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("User retrieved successfully")
                .data(user)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse<UserResponseDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestPart("user") UserUpdateDTO userUpdateDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        UserResponseDTO updatedUser = userService.updateUser(id, userUpdateDTO, file);

        SuccessResponse<UserResponseDTO> response = SuccessResponse.<UserResponseDTO>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .data(updatedUser)
                .message("User updated successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);

        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("User deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }
}
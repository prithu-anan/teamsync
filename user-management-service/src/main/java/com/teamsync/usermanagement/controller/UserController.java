package com.teamsync.usermanagement.controller;

import com.teamsync.usermanagement.dto.UserCreationDTO;
import com.teamsync.usermanagement.dto.UserResponseDTO;
import com.teamsync.usermanagement.dto.UserUpdateDTO;
import com.teamsync.usermanagement.entity.Users;
import com.teamsync.usermanagement.dto.UserProjectDTO;
import com.teamsync.usermanagement.dto.DesignationUpdateDto;

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
@RequestMapping({"/users", "/api/users"})
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<SuccessResponse<Void>> createUser(@Valid @RequestBody UserCreationDTO userDto) {
        userService.createUser(userDto);

        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.CREATED.value())
                .status(HttpStatus.CREATED)
                .message("User created successfully")
                // .data(createdUser)
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

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse<UserResponseDTO>> updateUser(
            @Valid @RequestPart("user") UserUpdateDTO userUpdateDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        UserResponseDTO updatedUser = userService.updateUser(userUpdateDTO, file);

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
                .code(HttpStatus.NO_CONTENT.value())
                .status(HttpStatus.OK)
                .message("User deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/designation/{id}")
    // @PreAuthorize("@userAuthorizationService.isManager()")
    public ResponseEntity<SuccessResponse<UserResponseDTO>> updateDesignation(@PathVariable Long id,
            @Valid @RequestBody DesignationUpdateDto dto) {
        UserResponseDTO user = userService.updateDesignation(id, dto);

        SuccessResponse<UserResponseDTO> response = SuccessResponse.<UserResponseDTO>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .data(user)
                .message("Designation updated successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects")
    public ResponseEntity<SuccessResponse<List<UserProjectDTO>>> getCurrentUserProjects() {
        System.out.println("*****************8*****************");
        List<UserProjectDTO> userProjects = userService.getCurrentUserProjects();
        SuccessResponse<List<UserProjectDTO>> response = SuccessResponse.<List<UserProjectDTO>>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("User projects retrieved successfully")
                .data(userProjects)
                .metadata(Map.of("count", userProjects.size()))
                .build();

        return ResponseEntity.ok(response);
    }

    // Replace the bottom part of your UserController with these properly
    // implemented endpoints:

    // Fix the URL paths by removing redundant "/users" prefix
    @GetMapping("/projects/{id}")
    public ResponseEntity<SuccessResponse<UserResponseDTO>> findByIdProject(@PathVariable("id") Long id) {
        UserResponseDTO user = userService.findByIdProject(id);

        SuccessResponse<UserResponseDTO> response = SuccessResponse.<UserResponseDTO>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("User retrieved successfully for project")
                .data(user)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/email/{email}")
    public ResponseEntity<SuccessResponse<UserResponseDTO>> findByEmailProject(@PathVariable("email") String email) {
        UserResponseDTO user = userService.findByEmailProject(email);
        System.out.println(user);
        SuccessResponse<UserResponseDTO> response = SuccessResponse.<UserResponseDTO>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("User retrieved successfully for project")
                .data(user)
                .build();
        System.out.println("Response: " + response);
        return ResponseEntity.ok(response   );
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<SuccessResponse<UserResponseDTO>> findByEmail(@PathVariable("email") String email) {
        UserResponseDTO user = userService.findByEmailProject(email);
        SuccessResponse<UserResponseDTO> response = SuccessResponse.<UserResponseDTO>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("User retrieved successfully by email")
                .data(user)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current")
    public ResponseEntity<SuccessResponse<UserResponseDTO>> getCurrentUser() {
        Users currentUser = userService.getCurrentUser();
        UserResponseDTO userResponse = userService.getUser(currentUser.getId()); // Get full user data with image

        SuccessResponse<UserResponseDTO> response = SuccessResponse.<UserResponseDTO>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Current user retrieved successfully")
                .data(userResponse)
                .build();

        return ResponseEntity.ok(response);
    }

}
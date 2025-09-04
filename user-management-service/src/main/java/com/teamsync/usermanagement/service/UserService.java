package com.teamsync.usermanagement.service;

import com.teamsync.usermanagement.dto.UserCreationDTO;
import com.teamsync.usermanagement.dto.UserResponseDTO;
import com.teamsync.usermanagement.dto.UserUpdateDTO;
// import com.teamsync.usermanagement.client.ProjectClientMock;
import com.teamsync.usermanagement.dto.DesignationUpdateDto;
import com.teamsync.usermanagement.dto.ProjectDTO;
import com.teamsync.usermanagement.dto.UserProjectDTO;

import com.teamsync.usermanagement.client.ProjectClient; // Replace ProjectClientMock import

import com.teamsync.usermanagement.entity.Users;
import com.teamsync.usermanagement.event.UserDeletedEvent;
import com.teamsync.usermanagement.exception.NotFoundException;
import com.teamsync.usermanagement.exception.ResourceNotFoundException;
import com.teamsync.usermanagement.exception.UnauthorizedException;

import com.teamsync.usermanagement.mapper.UserMapper;

import com.teamsync.usermanagement.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final ProjectClient projectClient; // Replace ProjectClientMock with ProjectClient
    private final KafkaTemplate<String, UserDeletedEvent> kafkaTemplate;

    public void createUser(UserCreationDTO userDto) {
        if (userDto == null) {
            throw new IllegalArgumentException("User data cannot be null");
        }

        // Check if email already exists
        Users existingUser = userRepository.findByEmail(userDto.getEmail());
        if (existingUser != null) {
            throw new IllegalArgumentException("Email already exists: " + userDto.getEmail());
        }

        Users user = userMapper.toEntity(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        // return userMapper.toResponseDTO(savedUser);
    }

    public List<UserResponseDTO> getAllUsers() {
        List<Users> users = userRepository.findAll();

        return users.stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO getUser(Long id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        UserResponseDTO response = userMapper.toResponseDTO(user);

        // Add base64 image data if exists
        if (user.getProfilePictureData() != null) {
            String base64Image = Base64.getEncoder().encodeToString(user.getProfilePictureData());
            response.setProfilePicture("data:image/jpeg;base64," + base64Image);
        }

        return response;
    }

    public UserResponseDTO updateUser(UserUpdateDTO userUpdateDTO, MultipartFile file) {
        Users currentUser = getCurrentUser();
        Long id = currentUser.getId();
        Users existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userMapper.updateUserFromDTO(userUpdateDTO, existingUser);

        // Handle file upload
        if (file != null && !file.isEmpty()) {
            try {
                byte[] fileData = fileStorageService.storeFile(file);
                existingUser.setProfilePictureData(fileData);
                existingUser.setProfilePicture("stored_in_database");
            } catch (Exception e) {
                throw new RuntimeException("Failed to store profile picture: " + e.getMessage());
            }
        }

        Users updatedUser = userRepository.save(existingUser);

        UserResponseDTO response = userMapper.toResponseDTO(updatedUser);

        // Add base64 image data if exists
        if (updatedUser.getProfilePictureData() != null) {
            String base64Image = Base64.getEncoder().encodeToString(updatedUser.getProfilePictureData());
            response.setProfilePicture("data:image/jpeg;base64," + base64Image);
        }

        return response;
    }

    public void deleteUser(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        Users user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        // Check if user has created any projects
        try {
            Boolean hasCreatedProjects = projectClient.hasUserCreatedProjects(id);
            if (hasCreatedProjects != null && hasCreatedProjects) {
                throw new UnauthorizedException("Cannot delete user who has created projects.");
            }
        } catch (Exception e) {
            log.error("Failed to check created projects for user: {}", id, e);
            throw new RuntimeException("Failed to check user's created projects: " + e.getMessage());
        }

        // Delete user from database
        userRepository.deleteById(id);

        // Publish user deleted event
        UserDeletedEvent userDeletedEvent = new UserDeletedEvent(user.getId(), user.getEmail());
        log.info("Publishing UserDeletedEvent for user: {}", user.getId());
        kafkaTemplate.send("user-deleted", userDeletedEvent);
        log.info("UserDeletedEvent published successfully for user: {}", user.getId());
    }

    public Users getCurrentUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new NotFoundException("User not found with email: " + userEmail);
        }
        return user;
    }

    public UserResponseDTO updateDesignation(Long id, DesignationUpdateDto dto) {

        Users user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        if ("manager".equalsIgnoreCase(user.getDesignation())) {
            throw new UnauthorizedException("Cannot update designation for a manager user.");
        }

        user.setDesignation(dto.getDesignation());
        userRepository.save(user);
        return userMapper.toResponseDTO(user);
    }

    public String getCurrentUserDesignation() {
        Users currentUser = getCurrentUser();
        return currentUser.getDesignation();
    }

    public List<UserProjectDTO> getCurrentUserProjects() {
        Users currentUser = getCurrentUser();
        try {
            return projectClient.getUserProjectsByUserId(currentUser.getId());
        } catch (Exception e) {
            log.error("Failed to fetch projects for user: {}", currentUser.getId(), e);
            throw new RuntimeException("Failed to fetch user projects: " + e.getMessage());
        }
    }

    private UserResponseDTO buildUserResponseWithImage(Users user) {
        UserResponseDTO response = userMapper.toResponseDTO(user);

        // Add base64 image data if exists
        if (user.getProfilePictureData() != null) {
            String base64Image = Base64.getEncoder().encodeToString(user.getProfilePictureData());
            response.setProfilePicture("data:image/jpeg;base64," + base64Image);
        }

        return response;
    }

    public UserResponseDTO findByIdProject(Long id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        return buildUserResponseWithImage(user);
    }

    public UserResponseDTO findByEmailProject(String email) {
        System.out.println("Finding user by email: " + email);
        Users user = userRepository.findByEmail(email);
        if (user == null) {
            throw new NotFoundException("User not found with email: " + email);
        }
        return buildUserResponseWithImage(user);
    }

    // Add these methods to your existing UserService class

public boolean existsById(Long userId) {
    if (userId == null || userId <= 0) {
        return false;
    }
    return userRepository.existsById(userId);
}
}
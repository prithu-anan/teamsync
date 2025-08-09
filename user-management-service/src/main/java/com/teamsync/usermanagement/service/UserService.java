package com.teamsync.usermanagement.service;

import com.teamsync.usermanagement.dto.UserCreationDTO;
import com.teamsync.usermanagement.dto.UserResponseDTO;
import com.teamsync.usermanagement.dto.UserUpdateDTO;
import com.teamsync.usermanagement.entity.Users;
import com.teamsync.usermanagement.exception.ResourceNotFoundException;
import com.teamsync.usermanagement.mapper.UserMapper;
import com.teamsync.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public UserResponseDTO createUser(UserCreationDTO userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + userDto.getEmail());
        }

        Users user = userMapper.toEntity(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setJoinDate(LocalDate.now());
        
        Users savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    public List<UserResponseDTO> getAllUsers() {
        List<Users> users = userRepository.findAll();
        return userMapper.toResponseDTOList(users);
    }

    public UserResponseDTO getUser(Long id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        UserResponseDTO response = userMapper.toResponseDTO(user);
        
        // Add base64 image data if exists
        if (user.getProfilePictureData() != null) {
            String base64Image = Base64.getEncoder().encodeToString(user.getProfilePictureData());
            response.setProfilePicture("data:image/jpeg;base64," + base64Image);
        }
        
        return response;
    }

    public UserResponseDTO updateUser(Long id, UserUpdateDTO userUpdateDTO, MultipartFile file) {
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
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
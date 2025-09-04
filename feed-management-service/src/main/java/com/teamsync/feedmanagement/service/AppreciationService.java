package com.teamsync.feedmanagement.service;

import com.teamsync.feedmanagement.dto.AppreciationCreateDTO;
import com.teamsync.feedmanagement.dto.AppreciationResponseDTO;
import com.teamsync.feedmanagement.dto.AppreciationUpdateDTO;
import com.teamsync.feedmanagement.entity.Appreciations;
// import com.teamsync.feedmanagement.entity.Users;
import com.teamsync.feedmanagement.dto.UserResponseDTO;

import com.teamsync.feedmanagement.exception.NotFoundException;
import com.teamsync.feedmanagement.exception.UnauthorizedException;
import com.teamsync.feedmanagement.mapper.AppreciationMapper;
import com.teamsync.feedmanagement.repository.AppreciationRepository;
// import com.teamsync.feedmanagement.repository.UserRepository;
import com.teamsync.feedmanagement.client.UserClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AppreciationService {

    private final AppreciationRepository appreciationRepository;
    // private final UserRepository userRepository;
    private final UserClient userClient;
    private final AppreciationMapper appreciationMapper;

    public List<AppreciationResponseDTO> getAllAppreciations() {
        List<Appreciations> appreciations = appreciationRepository.findAll();

        return appreciations.stream()
                .map(appreciationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public AppreciationResponseDTO getAppreciationById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Appreciation ID cannot be null");
        }

        Appreciations appreciation = appreciationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Appreciation not found with id: " + id));
        return appreciationMapper.toResponseDTO(appreciation);
    }

    public AppreciationResponseDTO createAppreciation(AppreciationCreateDTO createDTO, String userEmail) {
        if (createDTO == null) {
            throw new IllegalArgumentException("Appreciation data cannot be null");
        }

        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw new UnauthorizedException("User email is required for creating appreciation");
        }

           // Validate from user (authenticated user)
    UserResponseDTO fromUser = userClient.findByEmail(userEmail).getData(); // FIX: Extract data from SuccessResponse
    if (fromUser == null) {
        throw new UnauthorizedException("User not found with email: " + userEmail);
    }

        // Validate to user exists
        if (createDTO.getToUserId() == null) {
            throw new IllegalArgumentException("Recipient user ID cannot be null");
        }

    UserResponseDTO toUser = userClient.findById(createDTO.getToUserId()).getData(); // FIX: Extract data from SuccessResponse
    if (toUser == null) { // FIX: Handle null response properly
        throw new NotFoundException("Recipient user not found with id: " + createDTO.getToUserId());
    }



        // Prevent self-appreciation
        if (fromUser.getId().equals(toUser.getId())) {
            throw new IllegalArgumentException("Cannot create appreciation for yourself");
        }

        Appreciations appreciation = appreciationMapper.toEntity(createDTO);
        appreciation.setFromUser(fromUser.getId());
        appreciation.setToUser(toUser.getId());
        appreciation.setTimestamp(ZonedDateTime.now());
        Appreciations savedAppreciation = appreciationRepository.save(appreciation);
        return appreciationMapper.toResponseDTO(savedAppreciation);
    }

    public AppreciationResponseDTO updateAppreciation(Long id, AppreciationUpdateDTO updateDTO, String userEmail) {
        if (id == null) {
            throw new IllegalArgumentException("Appreciation ID cannot be null");
        }

        if (updateDTO == null) {
            throw new IllegalArgumentException("Update data cannot be null");
        }

        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw new UnauthorizedException("User email is required for updating appreciation");
        }

        Appreciations existingAppreciation = appreciationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Appreciation not found with id: " + id));

        // Check if the authenticated user is the creator of the appreciation
      // Check if the authenticated user is the creator of the appreciation
    UserResponseDTO authenticatedUser = userClient.findByEmail(userEmail).getData(); // FIX: Extract data from SuccessResponse
    if (authenticatedUser == null) {
        throw new UnauthorizedException("User not found with email: " + userEmail);
    }


        // if
        // (!existingAppreciation.getFromUser().getId().equals(authenticatedUser.getId()))
        // {
        // throw new SecurityException("You can only update appreciations that you
        // created");
        // }

        // Validate users exist if IDs are being updated
      if (updateDTO.getFromUserId() != null) {
        UserResponseDTO fromUser = userClient.findById(updateDTO.getFromUserId()).getData(); // FIX: Extract data from SuccessResponse
        if (fromUser == null) { // FIX: Handle null response properly
            throw new NotFoundException("From user not found with id: " + updateDTO.getFromUserId());
        }
        existingAppreciation.setFromUser(fromUser.getId());
    }

    if (updateDTO.getToUserId() != null) {
        UserResponseDTO toUser = userClient.findById(updateDTO.getToUserId()).getData(); // FIX: Extract data from SuccessResponse
        if (toUser == null) { // FIX: Handle null response properly
            throw new NotFoundException("To user not found with id: " + updateDTO.getToUserId());
        }

        // Prevent self-appreciation
        if (existingAppreciation.getFromUser().equals(toUser.getId())) {
            throw new IllegalArgumentException("Cannot create appreciation for yourself");
        }

        existingAppreciation.setToUser(toUser.getId());
    }
    
        // Update the entity with new values
        appreciationMapper.updateEntityFromDTO(updateDTO, existingAppreciation);
        Appreciations updatedAppreciation = appreciationRepository.save(existingAppreciation);
        return appreciationMapper.toResponseDTO(updatedAppreciation);
    }

    public void deleteAppreciation(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Appreciation ID cannot be null");
        }

        appreciationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Appreciation not found with id: " + id));

        appreciationRepository.deleteById(id);
    }
}
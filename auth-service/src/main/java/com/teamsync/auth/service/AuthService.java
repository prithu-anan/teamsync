package com.teamsync.auth.service;

import com.teamsync.auth.config.JwtProvider;
import com.teamsync.auth.dto.authDTO.*;
import com.teamsync.auth.dto.userDTO.UserCreationDTO;
import com.teamsync.auth.dto.userDTO.UserResponseDTO;
import com.teamsync.auth.entity.Users;
import com.teamsync.auth.exception.UserCreationException;
import com.teamsync.auth.mapper.AuthMapper;
import com.teamsync.auth.mapper.UserMapper;
import com.teamsync.auth.repository.UserRepository;
import com.teamsync.auth.service.RefreshTokenService;
import com.teamsync.auth.service.TokenBlacklistService;
import com.teamsync.auth.service.UserManagementClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthMapper authMapper;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetService passwordResetService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserManagementClient userManagementClient;

    @Transactional
    public UserResponseDTO registerUser(UserCreationDTO userCreationDTO, HttpServletRequest request) {
        log.info("Starting user registration process for email: {}", userCreationDTO.getEmail());
        
        // Check if email already exists
        Users existingUser = userRepository.findByEmail(userCreationDTO.getEmail());
        if (existingUser != null) {
            throw new IllegalArgumentException("Email is already used with another account");
        }

        // Convert DTO to entity using mapper
        Users newUser = userMapper.toEntity(userCreationDTO);

        // Encode password
        newUser.setPassword(passwordEncoder.encode(userCreationDTO.getPassword()));

        // Create full user profile in user-management-service FIRST
        // If this fails, the entire transaction will rollback
        log.info("Attempting to create user in user-management-service for: {}", userCreationDTO.getEmail());
        try {
            userManagementClient.createUserInUserManagement(userCreationDTO);
            log.info("User created successfully in user-management-service: {}", userCreationDTO.getEmail());
        } catch (Exception e) {
            log.error("Failed to create user in user-management-service: {}", e.getMessage());
            // This will cause the entire transaction to rollback
            throw new UserCreationException("Failed to create user profile. Please try again.", e);
        }

        // Save minimal user in auth service database
        // This will only happen if user management service succeeded
        log.info("Saving user to auth service database for: {}", userCreationDTO.getEmail());
        Users savedUser = userRepository.save(newUser);
        log.info("User created successfully in auth-service: {}", userCreationDTO.getEmail());

        // Create authentication and generate tokens
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                savedUser.getEmail(),
                savedUser.getPassword()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("User registration completed successfully for: {}", userCreationDTO.getEmail());
        return userMapper.toResponseDTO(savedUser);
    }

    @Transactional
    public AuthResponseDTO loginUser(LoginRequestDTO loginRequestDTO, HttpServletRequest request) {
        // Authenticate user
        Authentication authentication = authenticate(loginRequestDTO.getEmail(), loginRequestDTO.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate tokens
        String token = jwtProvider.generateToken(authentication);

        // Get user details for response
        Users user = userRepository.findByEmail(loginRequestDTO.getEmail());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Update last login time
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String refreshToken = refreshTokenService.createRefreshToken(user, request);

        // Convert user to response DTO
        UserResponseDTO userResponseDTO = userMapper.toResponseDTO(user);

        log.info("User logged in successfully: {}", user.getEmail());
        return authMapper.toAuthResponseDTO(userResponseDTO, token, refreshToken);
    }

    public TokenRefreshResponseDTO refreshToken(String refreshToken) {
        return refreshTokenService.refreshToken(refreshToken);
    }

    @Transactional
    public void logout(String refreshToken, String jwtToken) {
        // Blacklist the JWT token
        if (jwtToken != null && !jwtToken.trim().isEmpty()) {
            tokenBlacklistService.blacklistToken(jwtToken);
        }

        // Revoke refresh token
        if (refreshToken != null && !refreshToken.trim().isEmpty()) {
            refreshTokenService.revokeToken(refreshToken);
        }

        // Clear security context
        SecurityContextHolder.clearContext();
        log.info("User logged out successfully");
    }

    // Add method to check if token is blacklisted
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistService.isTokenBlacklisted(token);
    }

    public UserResponseDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new SecurityException("Unauthorized access");
        }

        String email = authentication.getName();
        Users user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        return authMapper.toCurrentUserResponseDTO(user);
    }

    public void updateCurrentUser(UserUpdateRequestDTO requestDTO) {
        // For user profile updates, delegate to user-management-service
        // Auth service only handles authentication-related updates
        try {
            userManagementClient.updateUserProfile(requestDTO);
            log.info("User profile updated via user-management-service");
        } catch (Exception e) {
            log.error("Failed to update user profile via user-management-service: {}", e.getMessage());
            throw new RuntimeException("Failed to update user profile");
        }
    }

    public void changePassword(PasswordChangeRequestDTO requestDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!passwordEncoder.matches(requestDTO.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(requestDTO.getNewPassword()));
        userRepository.save(user);
        
        log.info("Password changed successfully for user: {}", email);
    }

    @Transactional
    public void requestPasswordReset(PasswordResetRequestDTO request) {
        passwordResetService.requestPasswordReset(request);
    }

    @Transactional
    public void resetPassword(PasswordResetDTO resetRequest) {
        passwordResetService.resetPassword(resetRequest);
    }

    public boolean validateToken(String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            return false;
        }
        
        // Check if token is blacklisted
        if (isTokenBlacklisted(jwtToken)) {
            return false;
        }
        
        // Validate JWT token using JwtProvider
        return jwtProvider.validateToken(jwtToken);
    }

    private Authentication authenticate(String username, String password) {
        // Load user directly from repository
        Users user = userRepository.findByEmail(username);

        if (user == null) {
            throw new BadCredentialsException("Invalid username");
        }

        if (!user.getIsActive()) {
            throw new BadCredentialsException("Account is deactivated");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        // Create authorities list (empty for now, you can add roles later if needed)
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Create UserDetails object inline
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPassword(),
                        authorities
                );

        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }
}

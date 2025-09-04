package com.teamsync.feedmanagement.controller;

import com.teamsync.feedmanagement.dto.AppreciationCreateDTO;
import com.teamsync.feedmanagement.dto.AppreciationResponseDTO;
import com.teamsync.feedmanagement.dto.AppreciationUpdateDTO;
import com.teamsync.feedmanagement.response.SuccessResponse;

import com.teamsync.feedmanagement.service.AppreciationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/appreciations")
@RequiredArgsConstructor
public class AppreciationController {
        private final AppreciationService appreciationService;

        @GetMapping
        public ResponseEntity<SuccessResponse<List<AppreciationResponseDTO>>> getAllAppreciations() {
                List<AppreciationResponseDTO> appreciations = appreciationService.getAllAppreciations();
                SuccessResponse<List<AppreciationResponseDTO>> response = SuccessResponse
                                .<List<AppreciationResponseDTO>>builder()
                                .code(HttpStatus.OK.value())
                                .status(HttpStatus.OK)
                                .message("Appreciations retrieved successfully")
                                .data(appreciations)
                                .metadata(Map.of("count", appreciations.size()))
                                .build();
                return ResponseEntity.ok(response);
        }

        @GetMapping("/{id}")
        public ResponseEntity<SuccessResponse<AppreciationResponseDTO>> getAppreciationById(
                        @PathVariable Long id) {

                AppreciationResponseDTO appreciation = appreciationService.getAppreciationById(id);

                SuccessResponse<AppreciationResponseDTO> response = SuccessResponse.<AppreciationResponseDTO>builder()
                                .code(HttpStatus.OK.value())
                                .status(HttpStatus.OK)
                                .message("Appreciation retrieved successfully")
                                .data(appreciation)
                                .build();

                return ResponseEntity.ok(response);
        }

        @PostMapping
        public ResponseEntity<SuccessResponse<AppreciationResponseDTO>> createAppreciation(
                        @Valid @RequestBody AppreciationCreateDTO createDTO) {

                // Authentication authentication =
                // SecurityContextHolder.getContext().getAuthentication();
                // String userEmail = authentication.getName();
                String userEmail = "a@b.com";

                AppreciationResponseDTO createdAppreciation = appreciationService.createAppreciation(createDTO,
                                userEmail);

                SuccessResponse<AppreciationResponseDTO> response = SuccessResponse.<AppreciationResponseDTO>builder()
                                .code(HttpStatus.CREATED.value())
                                .status(HttpStatus.CREATED)
                                .message("Appreciation created successfully")
                                .data(createdAppreciation)
                                .build();

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @PutMapping("/{id}")
        public ResponseEntity<SuccessResponse<AppreciationResponseDTO>> updateAppreciation(
                        @PathVariable Long id,
                        @Valid @RequestBody AppreciationUpdateDTO updateDTO) {

                // Authentication authentication =
                // SecurityContextHolder.getContext().getAuthentication();
                // String userEmail = authentication.getName();
                String userEmail = "a@b.com";

                AppreciationResponseDTO updatedAppreciation = appreciationService.updateAppreciation(id, updateDTO,
                                userEmail);

                SuccessResponse<AppreciationResponseDTO> response = SuccessResponse.<AppreciationResponseDTO>builder()
                                .code(HttpStatus.OK.value())
                                .status(HttpStatus.OK)
                                .message("Appreciation updated successfully")
                                .data(updatedAppreciation)
                                .build();
                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<SuccessResponse<Void>> deleteAppreciation(
                        @PathVariable Long id) {

                appreciationService.deleteAppreciation(id);
                SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                                .code(HttpStatus.NO_CONTENT.value())
                                .status(HttpStatus.OK)
                                .message("Appreciation deleted successfully")
                                .build();
                return ResponseEntity.ok(response);
        }
}
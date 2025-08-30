package com.teamsync.feedmanagement.controller;

import com.teamsync.feedmanagement.dto.ReactionCreateRequestDTO;
import com.teamsync.feedmanagement.dto.ReactionResponseDTO;
import com.teamsync.feedmanagement.response.SuccessResponse;
import com.teamsync.feedmanagement.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/feedposts/{id}/reactions")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;

    @GetMapping
    public ResponseEntity<SuccessResponse<List<ReactionResponseDTO>>> getReactions(
            @PathVariable Long id) {
        List<ReactionResponseDTO> reactions = reactionService.getAllReactions(id);
        SuccessResponse<List<ReactionResponseDTO>> resp = SuccessResponse.<List<ReactionResponseDTO>>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Reactions fetched successfully")
                .data(reactions)
                .build();
        return ResponseEntity.ok(resp);
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<Void>> addReaction(
            @PathVariable Long id,
            @Valid @RequestBody ReactionCreateRequestDTO request) {
        reactionService.addReaction(id, request);
        SuccessResponse<Void> resp = SuccessResponse.<Void>builder()
                .code(HttpStatus.CREATED.value())
                .status(HttpStatus.CREATED)
                .message("Reaction added successfully")
//                .data(reaction)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @DeleteMapping
    public ResponseEntity<SuccessResponse<Void>> removeReaction(
            @PathVariable Long id,
            @RequestParam("user_id") Long userId,
            @RequestParam("reaction_type") String reactionType) {
        reactionService.removeReaction(id, userId, reactionType);
        SuccessResponse<Void> resp = SuccessResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .status(HttpStatus.OK)
                .message("Reaction removed successfully")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(resp);
    }
    @PutMapping
    public ResponseEntity<SuccessResponse<Void>> updateReaction(
            @PathVariable Long id,
            @Valid @RequestBody ReactionCreateRequestDTO request) {
        reactionService.updateReaction(id, request);
        SuccessResponse<Void> resp = SuccessResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Reaction updated successfully")
                .build();
        return ResponseEntity.ok(resp);
    }
}

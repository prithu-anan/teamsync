package com.teamsync.feedmanagement.controller;

import com.teamsync.feedmanagement.dto.PollVoteCreationDTO;
import com.teamsync.feedmanagement.dto.PollVoteResponseDTO;
import com.teamsync.feedmanagement.dto.PollVoteUpdateDTO;
import com.teamsync.feedmanagement.response.SuccessResponse;
import com.teamsync.feedmanagement.service.PollVoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pollvotes")
@RequiredArgsConstructor
public class PollVoteController {

    private final PollVoteService pollVotesService;

    @GetMapping
    public ResponseEntity<SuccessResponse<List<PollVoteResponseDTO>>> getAllPollVotes() {
        List<PollVoteResponseDTO> pollVotes = pollVotesService.getAllPollVotes();
        SuccessResponse<List<PollVoteResponseDTO>> resp = SuccessResponse.<List<PollVoteResponseDTO>>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Poll votes fetched successfully")
                .data(pollVotes)
                .build();
        return ResponseEntity.ok(resp);
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<Void>> createPollVote(
            @Valid @RequestBody PollVoteCreationDTO request) {
        // Authentication authentication =
        // SecurityContextHolder.getContext().getAuthentication();
        // String userEmail = authentication.getName();
        String userEmail = "a@b.com";

        pollVotesService.createPollVote(request, userEmail);
        SuccessResponse<Void> resp = SuccessResponse.<Void>builder()
                .code(HttpStatus.CREATED.value())
                .status(HttpStatus.CREATED)
                .message("Poll vote created successfully")
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<PollVoteResponseDTO>> getPollVoteById(@PathVariable Long id) {
        PollVoteResponseDTO response = pollVotesService.getPollVoteById(id);
        SuccessResponse<PollVoteResponseDTO> resp = SuccessResponse.<PollVoteResponseDTO>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Poll vote fetched successfully")
                .data(response)
                .build();
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> updatePollVote(
            @PathVariable Long id,
            @Valid @RequestBody PollVoteUpdateDTO request) {
        pollVotesService.updatePollVote(id, request);
        SuccessResponse<Void> resp = SuccessResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Poll vote updated successfully")
                .build();
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> deletePollVote(@PathVariable Long id) {
        pollVotesService.deletePollVote(id);
        SuccessResponse<Void> resp = SuccessResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .status(HttpStatus.OK)
                .message("Poll vote deleted successfully")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(resp);
    }
}

package com.teamsync.feedmanagement.controller;

import com.teamsync.feedmanagement.dto.CommentCreateRequestDTO;
import com.teamsync.feedmanagement.dto.CommentResponseDTO;
import com.teamsync.feedmanagement.dto.CommentUpdateRequestDTO;
import com.teamsync.feedmanagement.dto.ReplyCreateRequestDTO;
import com.teamsync.feedmanagement.dto.ReactionCreateRequestDTO;
import com.teamsync.feedmanagement.dto.ReactionResponseDTO;
import com.teamsync.feedmanagement.response.SuccessResponse;
import com.teamsync.feedmanagement.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/feedposts")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/{id}/comments")
    public ResponseEntity<SuccessResponse<List<CommentResponseDTO>>> getAllComments(@PathVariable("id") Long postId) {
        List<CommentResponseDTO> comments = commentService.getAllCommentsByPostId(postId);
        return ResponseEntity.ok(
                SuccessResponse.<List<CommentResponseDTO>>builder()
                        .code(HttpStatus.OK.value())
                        .status(HttpStatus.OK)
                        .message("All Comments fetched successfully")
                        .data(comments)
                        .build()
        );
    }

    @GetMapping("/{id}/comments/{commentId}")
    public ResponseEntity<SuccessResponse<CommentResponseDTO>> getComment(
            @PathVariable("id") Long postId,
            @PathVariable("commentId") Long commentId) {
        CommentResponseDTO comment = commentService.getCommentById(postId, commentId);
        return ResponseEntity.ok(
                SuccessResponse.<CommentResponseDTO>builder()
                        .code(HttpStatus.OK.value())
                        .status(HttpStatus.OK)
                        .message("Comment fetched successfully")
                        .data(comment)
                        .build()
        );
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<SuccessResponse<Void>> createComment(
            @PathVariable("id") Long postId,
            @Valid @RequestBody CommentCreateRequestDTO requestDTO) {
        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // String userEmail = authentication.getName();
        String userEmail = "a@b.com";

        commentService.createComment(postId, requestDTO, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                SuccessResponse.<Void>builder()
                        .code(HttpStatus.CREATED.value())
                        .status(HttpStatus.CREATED)
                        .message("Comment created successfully")
//                        .data(comment)
                        .build()
        );
    }

    @PutMapping("/{id}/comments/{commentId}")
    public ResponseEntity<SuccessResponse<Void>> updateComment(
            @PathVariable("id") Long postId,
            @PathVariable("commentId") Long commentId,
            @Valid @RequestBody CommentUpdateRequestDTO requestDTO) {
        commentService.updateComment(postId, commentId, requestDTO);
        return ResponseEntity.ok(
                SuccessResponse.<Void>builder()
                        .code(HttpStatus.OK.value())
                        .status(HttpStatus.OK)
                        .message("Comment updated successfully")
//                        .data(comment)
                        .build()
        );
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<SuccessResponse<Void>> deleteComment(
            @PathVariable("id") Long postId,
            @PathVariable("commentId") Long commentId) {
        commentService.deleteComment(postId, commentId);
        return ResponseEntity.ok(
                SuccessResponse.<Void>builder()
                        .code(HttpStatus.NO_CONTENT.value())
                        .status(HttpStatus.OK)
                        .message("Comment deleted successfully")
                        .build()
        );
    }

    //    ------------------------------------------------
    @GetMapping("/{postId}/comments/{commentId}/reactions")
    public ResponseEntity<SuccessResponse<List<ReactionResponseDTO>>> getAllCommentReactions(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId) {
        List<ReactionResponseDTO> reactions = commentService.getAllReactionsByCommentId(postId, commentId);
        return ResponseEntity.ok(
                SuccessResponse.<List<ReactionResponseDTO>>builder()
                        .code(HttpStatus.OK.value())
                        .status(HttpStatus.OK)
                        .message("Comment reactions fetched successfully")
                        .data(reactions)
                        .build()
        );
    }

    @PostMapping("/{postId}/comments/{commentId}/reactions")
    public ResponseEntity<SuccessResponse<Void>> addCommentReaction(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            @Valid @RequestBody ReactionCreateRequestDTO requestDTO) {
        commentService.addReactionToComment(postId, commentId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                SuccessResponse.<Void>builder()
                        .code(HttpStatus.CREATED.value())
                        .status(HttpStatus.CREATED)
                        .message("Reaction added to comment successfully")
//                        .data(reaction)
                        .build()
        );
    }

    @DeleteMapping("/{postId}/comments/{commentId}/reactions")
    public ResponseEntity<SuccessResponse<Void>> removeCommentReaction(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            @RequestParam("user_id") Long userId,
            @RequestParam("reaction_type") String reactionType) {
        commentService.removeReactionFromComment(postId, commentId, userId, reactionType);
        return ResponseEntity.ok(
                SuccessResponse.<Void>builder()
                        .code(HttpStatus.NO_CONTENT.value())
                        .status(HttpStatus.OK)
                        .message("Reaction removed from comment successfully")
                        .build()
        );
    }

    @GetMapping("/{id}/comments/{commentId}/replies")
    public ResponseEntity<SuccessResponse<List<CommentResponseDTO>>> getAllReplies(
            @PathVariable("id") Long postId,
            @PathVariable("commentId") Long commentId) {
        List<CommentResponseDTO> replies = commentService.getAllRepliesByCommentId(postId, commentId);
        return ResponseEntity.ok(
                SuccessResponse.<List<CommentResponseDTO>>builder()
                        .code(HttpStatus.OK.value())
                        .status(HttpStatus.OK)
                        .message("All replies fetched successfully")
                        .data(replies)
                        .build()
        );
    }

    @PostMapping("/{id}/comments/{commentId}/replies")
    public ResponseEntity<SuccessResponse<Void>> addReplyToComment(
            @PathVariable("id") Long postId,
            @PathVariable("commentId") Long commentId,
            @Valid @RequestBody ReplyCreateRequestDTO requestDTO) {
        commentService.addReplyToComment(postId, commentId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                SuccessResponse.<Void>builder()
                        .code(HttpStatus.CREATED.value())
                        .status(HttpStatus.CREATED)
                        .message("Reply added to comment successfully")
                        .build()
        );
    }

}

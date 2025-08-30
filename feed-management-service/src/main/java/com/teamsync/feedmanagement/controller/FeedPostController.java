
package com.teamsync.feedmanagement.controller;

import com.teamsync.feedmanagement.dto.*;
import com.teamsync.feedmanagement.response.SuccessResponse;
import com.teamsync.feedmanagement.service.FeedPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/feedposts")
@RequiredArgsConstructor
public class FeedPostController {

    private final FeedPostService feedPostsService;

    @GetMapping
    public ResponseEntity<SuccessResponse<FeedPostPaginationResponseDTO>> getAllFeedPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(required = false, defaultValue = "text") String type) {
        
        FeedPostPaginationResponseDTO paginatedResponse = feedPostsService.getAllFeedPostsWithPagination(
                page, limit, sortBy, order, type);
        
        SuccessResponse<FeedPostPaginationResponseDTO> resp = SuccessResponse.<FeedPostPaginationResponseDTO>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Feed posts fetched successfully")
                .data(paginatedResponse)
                .build();
        return ResponseEntity.ok(resp);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse<FeedPostResponseDTO>> createFeedPost(
            @Valid @RequestPart("feedPost") FeedPostCreateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {


        FeedPostResponseDTO createdPost = feedPostsService.createFeedPost(request, files);

        SuccessResponse<FeedPostResponseDTO> resp = SuccessResponse.<FeedPostResponseDTO>builder()
                .code(HttpStatus.CREATED.value())
                .status(HttpStatus.CREATED)
                .message("Feed post created successfully")
                .data(createdPost)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<FeedPostWithReactionDTO>> getFeedPostById(@PathVariable Long id) {
        FeedPostWithReactionDTO feedPost = feedPostsService.getFeedPostById(id);
        SuccessResponse<FeedPostWithReactionDTO> resp = SuccessResponse.<FeedPostWithReactionDTO>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Feed post fetched successfully")
                .data(feedPost)
                .build();
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> updateFeedPost(
            @PathVariable Long id,
            @Valid @RequestBody FeedPostUpdateRequest request) {
        feedPostsService.updateFeedPost(id, request);
        SuccessResponse<Void> resp = SuccessResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Feed post updated successfully")
//                .data(updatedPost)
                .build();
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> deleteFeedPost(@PathVariable Long id) {
        feedPostsService.deleteFeedPost(id);
        SuccessResponse<Void> resp = SuccessResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .status(HttpStatus.OK)
                .message("Feed post deleted successfully")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(resp);
    }
}

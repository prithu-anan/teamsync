
package com.teamsync.message_management_service.controller;

import com.teamsync.message_management_service.dto.FileCreationDTO;
import com.teamsync.message_management_service.dto.MessageCreationDTO;
import com.teamsync.message_management_service.dto.MessageResponseDTO;
import com.teamsync.message_management_service.dto.MessageUpdateDTO;

import com.teamsync.message_management_service.response.SuccessResponse;
import com.teamsync.message_management_service.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;




@RestController
@RequestMapping("/channels")
@RequiredArgsConstructor
public class MessageController {

        private final MessageService messageService;

        @GetMapping("/{channelId}/messages")
        public ResponseEntity<SuccessResponse<List<MessageResponseDTO>>> getChannelMessages(
                        @PathVariable Long channelId) {
                List<MessageResponseDTO> messages = messageService.getChannelMessages(channelId);
                SuccessResponse<List<MessageResponseDTO>> resp = SuccessResponse.<List<MessageResponseDTO>>builder()
                                .code(HttpStatus.OK.value())
                                .status(HttpStatus.OK)
                                .message("Messages fetched successfully")
                                .data(messages)
                                .build();
                return ResponseEntity.ok(resp);
        }

        @PostMapping("/{channelId}/messages")
        public ResponseEntity<SuccessResponse<Void>> createChannelMessage(
                        @PathVariable Long channelId,
                        @Valid @RequestBody MessageCreationDTO requestDto) {
                messageService.createChannelMessage(channelId, requestDto);
                SuccessResponse<Void> resp = SuccessResponse.<Void>builder()
                                .code(HttpStatus.CREATED.value())
                                .status(HttpStatus.CREATED)
                                .message("Message created successfully")
                                // .data(responseDto)
                                .build();
                return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        }

        @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<SuccessResponse<Void>> createMessageWithFiles(
                        @RequestParam(value = "channelId", required = false) Long channelId,
                        @RequestParam(value = "recipientId", required = false) Long recipientId,
                        @RequestParam(value = "threadParentId", required = false) Long threadParentId,
                        @RequestParam("files") MultipartFile[] files) {

                // Create FileCreationDTO list from MultipartFile array
                List<FileCreationDTO> fileDtos = java.util.Arrays.stream(files)
                                .map(file -> new FileCreationDTO(file))
                                .toList();

                // Create MessageCreationDTO with channelId from path variable
                MessageCreationDTO requestDto = new MessageCreationDTO(null, channelId, recipientId, threadParentId,
                                fileDtos);

                messageService.createMessageWithFiles(requestDto);

                SuccessResponse<Void> resp = SuccessResponse.<Void>builder()
                                .code(HttpStatus.CREATED.value())
                                .status(HttpStatus.CREATED)
                                .message("Message with files created successfully")
                                .build();
                return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        }

        @GetMapping("/{channelId}/messages/{messageId}")
        public ResponseEntity<SuccessResponse<MessageResponseDTO>> getChannelMessage(
                        @PathVariable Long channelId,
                        @PathVariable Long messageId) {
                MessageResponseDTO responseDto = messageService.getChannelMessage(channelId, messageId);
                SuccessResponse<MessageResponseDTO> resp = SuccessResponse.<MessageResponseDTO>builder()
                                .code(HttpStatus.OK.value())
                                .status(HttpStatus.OK)
                                .message("Message fetched successfully")
                                .data(responseDto)
                                .build();
                return ResponseEntity.ok(resp);
        }

        @PutMapping("/{channelId}/messages/{messageId}")
        public ResponseEntity<SuccessResponse<Void>> updateChannelMessage(
                        @PathVariable Long channelId,
                        @PathVariable Long messageId,
                        @Valid @RequestBody MessageUpdateDTO requestDto) {
                // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                // String userEmail = authentication.getName();
                String userEmail = "a@b.com";
                messageService.updateChannelMessage(channelId, messageId, requestDto, userEmail);
                SuccessResponse<Void> resp = SuccessResponse.<Void>builder()
                                .code(HttpStatus.OK.value())
                                .status(HttpStatus.OK)
                                .message("Message updated successfully")
                                // .data(responseDto)
                                .build();
                return ResponseEntity.ok(resp);
        }

        @DeleteMapping("/{channelId}/messages/{messageId}")
        public ResponseEntity<SuccessResponse<Void>> deleteChannelMessage(
                        @PathVariable Long channelId,
                        @PathVariable Long messageId) {
                messageService.deleteChannelMessage(channelId, messageId);
                SuccessResponse<Void> resp = SuccessResponse.<Void>builder()
                                .code(HttpStatus.NO_CONTENT.value())
                                .status(HttpStatus.OK)
                                .message("Message deleted successfully")
                                .build();
                return ResponseEntity.status(HttpStatus.OK).body(resp);
        }
}

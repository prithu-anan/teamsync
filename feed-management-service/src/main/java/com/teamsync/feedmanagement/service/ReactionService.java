package com.teamsync.feedmanagement.service;

import com.teamsync.feedmanagement.dto.ReactionCreateRequestDTO;
import com.teamsync.feedmanagement.dto.ReactionResponseDTO;
import com.teamsync.feedmanagement.entity.FeedPosts;
import com.teamsync.feedmanagement.entity.Reactions;
// import com.teamsync.feedmanagement.entity.Users;
import com.teamsync.feedmanagement.dto.UserResponseDTO;

import com.teamsync.feedmanagement.exception.NotFoundException;
import com.teamsync.feedmanagement.mapper.ReactionMapper;
import com.teamsync.feedmanagement.repository.FeedPostRepository;
import com.teamsync.feedmanagement.repository.ReactionRepository;
// import com.teamsync.feedmanagement.repository.UserRepository;
import com.teamsync.feedmanagement.client.UserClient;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final FeedPostRepository feedPostRepository;
    // private final UserRepository userRepository;
    private final UserClient userClient;
    private final ReactionMapper reactionMapper;

    public List<ReactionResponseDTO> getAllReactions(Long postId) {
        FeedPosts post = feedPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("FeedPost not found with id: " + postId));
        List<Reactions> reactions = reactionRepository.findByPostId(postId);
        return reactionMapper.reactionsResponseToDTO(reactions);
    }

    public ReactionResponseDTO addReaction(Long postId, ReactionCreateRequestDTO request) {
        FeedPosts post = feedPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("FeedPost not found with id: " + postId));

                  UserResponseDTO user = userClient.findById(request.getUserId()).getData(); // FIX: Extract data from SuccessResponse
    if (user == null) { // FIX: Handle null response properly
        throw new NotFoundException("User not found with id: " + request.getUserId());
    }

        Reactions.ReactionType reactionType;
        try {
            reactionType = Reactions.ReactionType.valueOf(request.getReactionType());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid reaction type: " + request.getReactionType());
        }

        // Check if user already has this reaction on this post
        List<Reactions> existingReactions = reactionRepository.findByUserIdAndPostId(request.getUserId(), postId);
        boolean reactionExists = existingReactions.stream()
                .anyMatch(r -> r.getReactionType() == reactionType);

        if (reactionExists) {
            throw new IllegalArgumentException("User already has this reaction on this post");
        }

        // Create new reaction
        Reactions reaction = Reactions.builder()
                .user(user.getId())
                .post(post)
                .reactionType(reactionType)
                .createdAt(ZonedDateTime.now())
                .build();

        Reactions savedReaction = reactionRepository.save(reaction);
        return reactionMapper.reactionResponseToDTO(savedReaction);
    }

    public void removeReaction(Long postId, Long userId, String reactionType) {
        // Verify post exists
        FeedPosts post = feedPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("FeedPost not found with id: " + postId));

        // Verify user exists
  // Verify user exists
    UserResponseDTO user = userClient.findById(userId).getData(); // FIX: Extract data from SuccessResponse
    if (user == null) { // FIX: Handle null response properly
        throw new NotFoundException("User not found with id: " + userId);
    }
        // Validate reaction type
        try {
            Reactions.ReactionType.valueOf(reactionType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid reaction type: " + reactionType);
        }

        // Find and remove the specific reaction
        List<Reactions> reactions = reactionRepository.findByUserIdAndPostId(userId, postId);

        Reactions targetReaction = reactions.stream()
                .filter(r -> r.getReactionType().name().equals(reactionType))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "Reaction not found for user " + userId + " on post " + postId + " with type " + reactionType));

        reactionRepository.delete(targetReaction);
    }

    public ReactionResponseDTO updateReaction(Long postId, ReactionCreateRequestDTO request) {
        FeedPosts post = feedPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("FeedPost not found with id: " + postId));
  UserResponseDTO user = userClient.findById(request.getUserId()).getData(); // FIX: Extract data from SuccessResponse
    if (user == null) { // FIX: Handle null response properly
        throw new NotFoundException("User not found with id: " + request.getUserId());
    }
        Reactions.ReactionType reactionType;
        try {
            reactionType = Reactions.ReactionType.valueOf(request.getReactionType());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid reaction type: " + request.getReactionType());
        }

        // Find existing reactions for this user on this post
        List<Reactions> existingReactions = reactionRepository.findByUserIdAndPostId(request.getUserId(), postId);

        // Remove all existing reactions for this user on this post
        reactionRepository.deleteAll(existingReactions);

        // Add the new reaction
        Reactions reaction = Reactions.builder()
                .user(user.getId())
                .post(post)
                .reactionType(reactionType)
                .createdAt(ZonedDateTime.now())
                .build();

        Reactions updatedReaction = reactionRepository.save(reaction);
        return reactionMapper.reactionResponseToDTO(updatedReaction);
    }
}
package com.teamsync.feedmanagement.service;

import com.teamsync.feedmanagement.dto.PollVoteCreationDTO;
import com.teamsync.feedmanagement.dto.PollVoteResponseDTO;
import com.teamsync.feedmanagement.dto.PollVoteUpdateDTO;
import com.teamsync.feedmanagement.entity.FeedPosts;
import com.teamsync.feedmanagement.entity.PollVotes;
// import com.teamsync.feedmanagement.entity.Users;
import com.teamsync.feedmanagement.dto.UserResponseDTO;

import com.teamsync.feedmanagement.exception.NotFoundException;
import com.teamsync.feedmanagement.mapper.PollVoteMapper;
import com.teamsync.feedmanagement.repository.FeedPostRepository;
import com.teamsync.feedmanagement.repository.PollVoteRepository;
// import com.teamsync.feedmanagement.repository.UserRepository;
import com.teamsync.feedmanagement.client.UserClient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class PollVoteService {
    @Autowired
    private PollVoteRepository pollVotesRepository;
    @Autowired
    private FeedPostRepository feedPostsRepository;
    // @Autowired
    // private UserRepository usersRepository;

    @Autowired
    private UserClient userClient;

    @Autowired
    private PollVoteMapper pollVotesMapper;

    public List<PollVoteResponseDTO> getAllPollVotes() {
        return pollVotesRepository.findAll().stream()
                .map(pollVotesMapper::toDTO)
                .collect(Collectors.toList());
    }

    public PollVoteResponseDTO getPollVoteById(Long id) {
        PollVotes pollVote = pollVotesRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Poll vote not found with id: " + id));

        return pollVotesMapper.toDTO(pollVote);

    }

    public PollVoteResponseDTO createPollVote(PollVoteCreationDTO request, String userEmail) {
    UserResponseDTO user = userClient.findByEmail(userEmail).getData(); // FIX: Extract data from SuccessResponse
        if (user == null) {
            throw new NotFoundException("User not found with email " + userEmail);
        }

        FeedPosts poll = feedPostsRepository.findById(request.getPollId())
                .orElseThrow(() -> new NotFoundException("Poll not found with id: " + request.getPollId()));

        // Validate that the post is actually a poll
        if (poll.getType() != FeedPosts.FeedPostType.poll) {
            throw new NotFoundException("The specified post is not a poll");
        }

        if (!isValidPollOption(poll.getPollOptions(), request.getSelectedOption())) {
            throw new NotFoundException("Invalid poll option selected " + request.getSelectedOption());
        }
        PollVotes pollVote = PollVotes.builder()
                .poll(poll)
                .user(user.getId())
                .selectedOption(request.getSelectedOption())
                .build();

        PollVotes savedPollVote = pollVotesRepository.save(pollVote);
        return pollVotesMapper.toDTO(savedPollVote);
    }

    public PollVoteResponseDTO updatePollVote(Long id, PollVoteUpdateDTO request) {
        PollVotes existingPollVote = pollVotesRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Poll vote not found with id: " + id));

        FeedPosts poll = feedPostsRepository.findById(request.getPollId())
                .orElseThrow(() -> new NotFoundException("Poll not found with id: " + request.getPollId()));

        UserResponseDTO user = userClient.findById(request.getUserId()).getData(); // FIX: Extract data from SuccessResponse
    if (user == null) { // FIX: Handle null response properly
        throw new NotFoundException("User not found with id: " + request.getUserId());
    }

        if (!isValidPollOption(poll.getPollOptions(), request.getSelectedOption())) {
            throw new NotFoundException("Invalid poll option selected " + request.getSelectedOption());
        }

        existingPollVote.setPoll(poll);
        existingPollVote.setUser(user.getId());
        existingPollVote.setSelectedOption(request.getSelectedOption());
        PollVotes updatedPollVote = pollVotesRepository.save(existingPollVote);
        return pollVotesMapper.toDTO(updatedPollVote);
    }

    public void deletePollVote(Long id) {
        if (!pollVotesRepository.existsById(id)) {
            throw new NotFoundException("Poll vote not found with id: " + id);
        }
        pollVotesRepository.deleteById(id);
    }

    private boolean isValidPollOption(String[] pollOptions, String selectedOption) {
        if (pollOptions == null || pollOptions.length == 0 || selectedOption == null) {
            return false;
        }
        String normalizedSelected = selectedOption.trim();
        boolean isValid = Arrays.stream(pollOptions)
                .filter(option -> option != null)
                .map(String::trim)
                .anyMatch(option -> {
                    return option.equalsIgnoreCase(normalizedSelected);

                });
        return isValid;
    }
}
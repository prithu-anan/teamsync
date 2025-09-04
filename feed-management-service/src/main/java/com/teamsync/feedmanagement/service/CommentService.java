package com.teamsync.feedmanagement.service;

import com.teamsync.feedmanagement.dto.CommentCreateRequestDTO;
import com.teamsync.feedmanagement.dto.CommentResponseDTO;
import com.teamsync.feedmanagement.dto.CommentUpdateRequestDTO;
import com.teamsync.feedmanagement.dto.ReplyCreateRequestDTO;
import com.teamsync.feedmanagement.dto.ReactionCreateRequestDTO;
import com.teamsync.feedmanagement.dto.ReactionDetailDTO;
import com.teamsync.feedmanagement.dto.ReactionResponseDTO;
import com.teamsync.feedmanagement.entity.Comments;
import com.teamsync.feedmanagement.entity.FeedPosts;
import com.teamsync.feedmanagement.entity.Reactions;
// import com.teamsync.feedmanagement.entity.Users;
import com.teamsync.feedmanagement.dto.UserResponseDTO;

import com.teamsync.feedmanagement.exception.NotFoundException;
import com.teamsync.feedmanagement.mapper.CommentMapper;
import com.teamsync.feedmanagement.mapper.ReactionMapper;
import com.teamsync.feedmanagement.repository.CommentReactionRepository;
import com.teamsync.feedmanagement.repository.CommentRepository;
import com.teamsync.feedmanagement.repository.FeedPostRepository;
// import com.teamsync.feedmanagement.repository.UserRepository;
import com.teamsync.feedmanagement.client.UserClient;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
        private final CommentRepository commentRepository;
        private final FeedPostRepository feedPostRepository;
        // private final UserRepository userRepository;
        private final UserClient userClient;
        private final CommentMapper commentMapper;
        private final ReactionMapper reactionMapper;
        private final CommentReactionRepository commentReactionRepository;

        public List<CommentResponseDTO> getAllCommentsByPostId(Long postId) {
                feedPostRepository.findById(postId)
                                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

                List<Comments> comments = commentRepository.findByPostIdOrderByTimestamp(postId);
                return commentMapper.toResponseDTOList(comments);
        }

        public CommentResponseDTO getCommentById(Long postId, Long commentId) {
                feedPostRepository.findById(postId)
                                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

                Comments comment = commentRepository.findByPostIdAndCommentId(postId, commentId)
                                .orElseThrow(() -> new NotFoundException(
                                                "Comment not found with id: " + commentId + " for post: " + postId));

                return commentMapper.toResponseDTO(comment);
        }

        public CommentResponseDTO createComment(Long postId, CommentCreateRequestDTO requestDTO, String userEmail) {
                FeedPosts post = feedPostRepository.findById(postId)
                                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

                UserResponseDTO author = userClient.getUserByEmail(userEmail);
                if (author == null) {
                        throw new NotFoundException("User not found ");
                }

                Comments parentComment = null;
                if (requestDTO.getParentCommentId() != null) {
                        parentComment = commentRepository.findById(requestDTO.getParentCommentId())
                                        .orElseThrow(() -> new NotFoundException("Parent comment not found with id: "
                                                        + requestDTO.getParentCommentId()));

                        parentComment.setReplyCount(parentComment.getReplyCount() + 1);
                        commentRepository.save(parentComment);
                }

                Comments comment = commentMapper.toEntity(requestDTO);
                comment.setPost(post);
                comment.setAuthor(author.getId());
                comment.setParentComment(parentComment);
                comment.setTimestamp(ZonedDateTime.now());
                comment.setReplyCount(0);

                Comments savedComment = commentRepository.save(comment);

                return commentMapper.toResponseDTO(savedComment);

                // return commentMapper.toResponseDTO(savedComment);
        }

        public CommentResponseDTO updateComment(Long postId, Long commentId, CommentUpdateRequestDTO requestDTO) {
                feedPostRepository.findById(postId)
                                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

                Comments existingComment = commentRepository.findByPostIdAndCommentId(postId, commentId)
                                .orElseThrow(() -> new NotFoundException(
                                                "Comment not found with id: " + commentId + " for post: " + postId));

                commentMapper.updateEntityFromDTO(requestDTO, existingComment);

                if (requestDTO.getParentCommentId() != null &&
                                !requestDTO.getParentCommentId()
                                                .equals(existingComment.getParentComment() != null
                                                                ? existingComment.getParentComment().getId()
                                                                : null)) {

                        Comments newParentComment = commentRepository.findById(requestDTO.getParentCommentId())
                                        .orElseThrow(() -> new NotFoundException("Parent comment not found with id: "
                                                        + requestDTO.getParentCommentId()));
                        existingComment.setParentComment(newParentComment);
                }

                if (requestDTO.getReactions() != null) {
                        updateCommentReactions(existingComment, requestDTO.getReactions());
                }

                Comments updatedComment = commentRepository.save(existingComment);
                return commentMapper.toResponseDTO(updatedComment);
        }

        public void deleteComment(Long postId, Long commentId) {
                feedPostRepository.findById(postId)
                                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

                Comments comment = commentRepository.findByPostIdAndCommentId(postId, commentId)
                                .orElseThrow(() -> new NotFoundException(
                                                "Comment not found with id: " + commentId + " for post: " + postId));

                if (comment.getParentComment() != null) {
                        Comments parentComment = comment.getParentComment();
                        parentComment.setReplyCount(Math.max(0, parentComment.getReplyCount() - 1));
                        commentRepository.save(parentComment);
                }

                commentReactionRepository.deleteByCommentId(commentId);
                commentRepository.delete(comment);
        }

        public void updateCommentReactions(Comments comment, List<ReactionDetailDTO> reactionDTOs) {
                List<Reactions> existingReaction = commentReactionRepository.findByCommentId(comment.getId());
                if (!existingReaction.isEmpty()) {
                        commentReactionRepository.deleteByCommentId(comment.getId());
                }
                for (ReactionDetailDTO reactionDTO : reactionDTOs) {
                        UserResponseDTO user = userClient.findById(reactionDTO.getUserId())
                                        .orElseThrow(() -> new NotFoundException(
                                                        "User not found with id: " + reactionDTO.getUserId()));

                        Reactions reaction = Reactions.builder()
                                        .comment(comment)
                                        .user(user.getId())
                                        .reactionType(Reactions.ReactionType
                                                        .valueOf(reactionDTO.getReactionType().toLowerCase()))
                                        .createdAt(reactionDTO.getCreatedAt() != null ? reactionDTO.getCreatedAt()
                                                        : ZonedDateTime.now())
                                        .build();

                        commentReactionRepository.save(reaction);
                }
        }

        public List<ReactionResponseDTO> getAllReactionsByCommentId(Long postId, Long commentId) {
                // Verify post exists
                feedPostRepository.findById(postId)
                                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

                // Verify comment exists and belongs to the post
                commentRepository.findByPostIdAndCommentId(postId, commentId)
                                .orElseThrow(() -> new NotFoundException(
                                                "Comment not found with id: " + commentId + " for post: " + postId));

                List<Reactions> reactions = commentReactionRepository.findByCommentId(commentId);
                return reactionMapper.reactionsResponseToDTO(reactions);
        }

        public ReactionResponseDTO addReactionToComment(Long postId, Long commentId,
                        ReactionCreateRequestDTO requestDTO) {
                // Verify post exists
                feedPostRepository.findById(postId)
                                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

                // Verify comment exists and belongs to the post
                Comments comment = commentRepository.findByPostIdAndCommentId(postId, commentId)
                                .orElseThrow(() -> new NotFoundException(
                                                "Comment not found with id: " + commentId + " for post: " + postId));

                UserResponseDTO user = userClient.findById(requestDTO.getUserId())
                                .orElseThrow(() -> new NotFoundException(
                                                "User not found with id: " + requestDTO.getUserId()));

                // Validate reaction type by attempting to parse it
                Reactions.ReactionType reactionType;
                try {
                        reactionType = Reactions.ReactionType.valueOf(requestDTO.getReactionType().toLowerCase());
                } catch (IllegalArgumentException e) {
                        // This will be caught by ValidationExceptionHandler as IllegalArgumentException
                        throw new IllegalArgumentException("Invalid reaction type: " + requestDTO.getReactionType());
                }

                // Check if user already has this specific reaction on this comment
                Optional<Reactions> existingReaction = commentReactionRepository
                                .findByUserIdAndCommentIdAndReactionType(
                                                user.getId(), commentId, reactionType);

                if (existingReaction.isPresent()) {
                        // This will be caught by ValidationExceptionHandler as IllegalArgumentException
                        throw new IllegalArgumentException("User already has this reaction on this comment");
                }

                // Create new reaction
                Reactions reaction = Reactions.builder()
                                .user(user.getId())
                                .comment(comment)
                                .reactionType(reactionType)
                                .createdAt(ZonedDateTime.now())
                                .build();
                Reactions savedReaction = commentReactionRepository.save(reaction);
                return reactionMapper.reactionResponseToDTO(savedReaction);
        }

        public void removeReactionFromComment(Long postId, Long commentId, Long userId, String reactionTypeStr) {
                // Verify post exists
                feedPostRepository.findById(postId)
                                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

                // Verify comment exists and belongs to the post
                commentRepository.findByPostIdAndCommentId(postId, commentId)
                                .orElseThrow(() -> new NotFoundException(
                                                "Comment not found with id: " + commentId + " for post: " + postId));

                // Verify user exists
                userClient.findById(userId)
                                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

                // Validate reaction type
                Reactions.ReactionType reactionType;
                try {
                        reactionType = Reactions.ReactionType.valueOf(reactionTypeStr.toLowerCase());
                } catch (IllegalArgumentException e) {
                        // This will be caught by ValidationExceptionHandler as IllegalArgumentException
                        throw new IllegalArgumentException("Invalid reaction type: " + reactionTypeStr);
                }

                // Check if the specific reaction exists
                Optional<Reactions> existingReaction = commentReactionRepository
                                .findByUserIdAndCommentIdAndReactionType(
                                                userId, commentId, reactionType);

                if (existingReaction.isEmpty()) {
                        throw new NotFoundException("Reaction not found for user " + userId + " on comment " + commentId
                                        + " with type " + reactionTypeStr);
                }

                // Delete the specific reaction - any database constraint violations will be
                // handled by DBExceptionHandler
                commentReactionRepository.deleteByUserIdAndCommentIdAndReactionType(userId, commentId, reactionType);
        }

        public List<CommentResponseDTO> getAllRepliesByCommentId(Long postId, Long commentId) {
                // Verify post exists
                feedPostRepository.findById(postId)
                                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

                // Verify parent comment exists and belongs to the post
                commentRepository.findByPostIdAndCommentId(postId, commentId)
                                .orElseThrow(() -> new NotFoundException(
                                                "Comment not found with id: " + commentId + " for post: " + postId));

                List<Comments> replies = commentRepository.findRepliesByPostIdAndParentCommentId(postId, commentId);
                return commentMapper.toResponseDTOList(replies);
        }

        public CommentResponseDTO addReplyToComment(Long postId, Long commentId, ReplyCreateRequestDTO requestDTO) {
                // Verify post exists
                FeedPosts post = feedPostRepository.findById(postId)
                                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

                // Verify parent comment exists and belongs to the post
                Comments parentComment = commentRepository.findByPostIdAndCommentId(postId, commentId)
                                .orElseThrow(() -> new NotFoundException(
                                                "Comment not found with id: " + commentId + " for post: " + postId));

                // Verify user exists
                UserResponseDTO author = userClient.findById(requestDTO.getAuthor_id())
                                .orElseThrow(() -> new NotFoundException(
                                                "User not found with id: " + requestDTO.getAuthor_id()));

                // Create reply comment
                Comments reply = Comments.builder()
                                .post(post)
                                .author(author.getId())
                                .content(requestDTO.getContent())
                                .parentComment(parentComment)
                                .timestamp(ZonedDateTime.now())
                                .replyCount(0)
                                .build();

                // Increment parent comment's reply count
                parentComment.setReplyCount(parentComment.getReplyCount() + 1);

                // Save both entities
                commentRepository.save(parentComment);
                Comments savedReply = commentRepository.save(reply);

                return commentMapper.toResponseDTO(savedReply);
        }
}

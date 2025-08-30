package com.teamsync.feedmanagement.repository;

import com.teamsync.feedmanagement.entity.Reactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentReactionRepository extends JpaRepository<Reactions, Long> {

    // Find all reactions for a specific comment
    @Query("SELECT r FROM Reactions r WHERE r.comment.id = :commentId")
    List<Reactions> findByCommentId(@Param("commentId") Long commentId);

    // Delete all reactions for a specific comment
    @Modifying
    @Query("DELETE FROM Reactions r WHERE r.comment.id = :commentId")
    void deleteByCommentId(@Param("commentId") Long commentId);

    // Find reaction by user and comment
    @Query("SELECT r FROM Reactions r WHERE r.user = :userId AND r.comment.id = :commentId")
    List<Reactions> findByUserIdAndCommentId(@Param("userId") Long userId, @Param("commentId") Long commentId);

    // Find specific reaction by user, comment, and reaction type
    @Query("SELECT r FROM Reactions r WHERE r.user = :userId AND r.comment.id = :commentId AND r.reactionType = :reactionType")
    Optional<Reactions> findByUserIdAndCommentIdAndReactionType(
            @Param("userId") Long userId,
            @Param("commentId") Long commentId,
            @Param("reactionType") Reactions.ReactionType reactionType);

    // Delete specific reaction by user, comment, and reaction type
    @Modifying
    @Query("DELETE FROM Reactions r WHERE r.user = :userId AND r.comment.id = :commentId AND r.reactionType = :reactionType")
    void deleteByUserIdAndCommentIdAndReactionType(
            @Param("userId") Long userId,
            @Param("commentId") Long commentId,
            @Param("reactionType") Reactions.ReactionType reactionType);
}

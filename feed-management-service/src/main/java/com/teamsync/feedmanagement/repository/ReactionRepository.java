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
public interface ReactionRepository extends JpaRepository<Reactions, Long> {



    // Find all reactions for a specific post
    @Query("SELECT r FROM Reactions r WHERE r.post.id = :postId")
    List<Reactions> findByPostId(@Param("postId") Long postId);

    // Delete all reactions for a specific post
    @Modifying
    @Query("DELETE FROM Reactions r WHERE r.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);

    // Find reaction by user and post (to prevent duplicate reactions)
    @Query("SELECT r FROM Reactions r WHERE r.user = :userId AND r.post.id = :postId")
    List<Reactions> findByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    // Find specific reaction by user, post, and reaction type
    @Query("SELECT r FROM Reactions r WHERE r.user = :userId AND r.post.id = :postId AND r.reactionType = :reactionType")
    Optional<Reactions> findByUserIdAndPostIdAndReactionType(
            @Param("userId") Long userId,
            @Param("postId") Long postId,
            @Param("reactionType") Reactions.ReactionType reactionType);

    // Delete specific reaction by user, post, and reaction type
    @Modifying
    @Query("DELETE FROM Reactions r WHERE r.user = :userId AND r.post.id = :postId AND r.reactionType = :reactionType")
    void deleteByUserIdAndPostIdAndReactionType(
            @Param("userId") Long userId,
            @Param("postId") Long postId,
            @Param("reactionType") Reactions.ReactionType reactionType);
}
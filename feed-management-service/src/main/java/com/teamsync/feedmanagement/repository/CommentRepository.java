package com.teamsync.feedmanagement.repository;

import com.teamsync.feedmanagement.entity.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comments, Long> {

    @Query("SELECT c FROM Comments c WHERE c.post.id = :postId ORDER BY c.timestamp ASC")
    List<Comments> findByPostIdOrderByTimestamp(@Param("postId") Long postId);

    @Query("SELECT c FROM Comments c WHERE c.post.id = :postId AND c.id = :commentId")
    Optional<Comments> findByPostIdAndCommentId(@Param("postId") Long postId, @Param("commentId") Long commentId);

    @Query("SELECT COUNT(c) FROM Comments c WHERE c.parentComment.id = :parentId")
    int countRepliesByParentId(@Param("parentId") Long parentId);

    @Query("SELECT c FROM Comments c WHERE c.post.id = :postId AND c.parentComment.id = :parentCommentId ORDER BY c.timestamp ASC")
    List<Comments> findRepliesByPostIdAndParentCommentId(@Param("postId") Long postId, @Param("parentCommentId") Long parentCommentId);
}
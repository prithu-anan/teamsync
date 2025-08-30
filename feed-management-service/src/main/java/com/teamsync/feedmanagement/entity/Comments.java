package com.teamsync.feedmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private FeedPosts post;

    @Column(name = "author_id", nullable = false)
    private Long author;

    @Column(nullable = false)
    private String content;

    @Builder.Default
    @Column(nullable = false)
    private ZonedDateTime timestamp = ZonedDateTime.now();

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comments parentComment;

    @Builder.Default
    @Column(name = "reply_count", nullable = false)
    private int replyCount = 0;
}
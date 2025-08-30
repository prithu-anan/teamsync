
package com.teamsync.feedmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "feedposts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedPosts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedPostType type;
    
    @Column(name = "author_id", nullable = false)
    private Long author;

    @Column(nullable = false)
    @Builder.Default
    private String content = "";  

    // Use PostgreSQL array type directly
    @Column(name = "poll_options", columnDefinition = "text[]")
    private String[] pollOptions;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @Column(name = "event_date")
    private LocalDate eventDate;

    // Use PostgreSQL array type directly
    @Column(name = "media_urls", columnDefinition = "text[]")
    private String[] mediaUrls;

    @Builder.Default
    @Column(name = "is_ai_generated", nullable = false)
    private boolean isAiGenerated = false;

    @Column(name = "ai_summary")
    private String aiSummary;

    public enum FeedPostType {
        text, photo, event, appreciation, poll, birthday, highlight
    }
}
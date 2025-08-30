package com.teamsync.feedmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pollvotes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollVotes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "poll_id", nullable = false)
    private FeedPosts poll;

    @Column(name = "user_id", nullable = false)
    private Long user;

    @Column(name = "selected_option", nullable = false)
    private String selectedOption;
}
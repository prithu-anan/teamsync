package com.teamsync.message_management_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name = "reactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reactions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long user;

    @Enumerated(EnumType.STRING)
    @Column(name="reaction_type",nullable = false)
    private ReactionType reactionType;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();



    @ManyToOne
    @JoinColumn(name = "message_id")
    private Messages message;



    public enum ReactionType {
        like, love, haha, wow, sad, angry, celebrate, support, insightful
    }
}
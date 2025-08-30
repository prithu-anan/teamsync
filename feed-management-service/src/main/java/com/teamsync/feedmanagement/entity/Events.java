package com.teamsync.feedmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Events {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "parent_post_id")
    private FeedPosts parentPost;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    @Column(nullable = false)
    private LocalDate date;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "participants", columnDefinition = "bigint[]")
    private List<Long> participants;

    @Column(name = "tentative_starting_date")
    private LocalDate tentativeStartingDate;

    public enum EventType {
        Birthday, Workiversary, Outing
    }
}
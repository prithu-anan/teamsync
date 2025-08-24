package com.teamsync.projectmanagementservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name = "projectmembers")
@IdClass(ProjectMemberId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMembers {
    @Id
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Projects project;

    @Id
    @Column(name = "user_id", nullable = false)
    private Long user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectRole role;

    @Builder.Default
    @Column(name = "joined_at", nullable = false)
    private ZonedDateTime joinedAt = ZonedDateTime.now();

    public enum ProjectRole {
        owner, admin, member, guest, viewer
    }
}
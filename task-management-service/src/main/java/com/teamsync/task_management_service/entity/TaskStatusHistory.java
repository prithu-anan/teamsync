package com.teamsync.task_management_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name = "taskstatushistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskStatusHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Tasks task;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tasks.TaskStatus status;
    
    @Column(name = "changed_by", nullable = false)
    private Long changedBy;
    
    @Builder.Default
    @Column(name = "changed_at", nullable = false)
    private ZonedDateTime changedAt = ZonedDateTime.now();
    
    @Column(columnDefinition = "TEXT")
    private String comment;
}
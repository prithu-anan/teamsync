package com.teamsync.task_management_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tasks {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;
    
    @Column(name = "deadline")
    private ZonedDateTime deadline;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private TaskPriority priority;
    
    @Column(name = "time_estimate", length = 50)
    private String timeEstimate;
    
    @Column(name = "ai_time_estimate", length = 50)
    private String aiTimeEstimate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "ai_priority", length = 10)
    private TaskPriority aiPriority;
    
    @Column(name = "smart_deadline")
    private ZonedDateTime smartDeadline;
    
    @Column(name = "project_id", nullable = false)
    private Long project;
    
    @Column(name = "assigned_to")
    private Long assignedTo;
    
    @Column(name = "assigned_by")
    private Long assignedBy;
    
    @Column(name = "assigned_at")
    private ZonedDateTime assignedAt;
    
    @ManyToOne
    @JoinColumn(name = "parent_task_id")
    private Tasks parentTask;
    
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "attachments", columnDefinition = "text[]")
    private List<String> attachments;
    
    @Column(name = "tentative_starting_date")
    private LocalDate tentativeStartingDate;
    
    public enum TaskStatus {
        backlog, todo, in_progress, in_review, blocked, completed
    }
    
    public enum TaskPriority {
        low, medium, high, urgent
    }
}
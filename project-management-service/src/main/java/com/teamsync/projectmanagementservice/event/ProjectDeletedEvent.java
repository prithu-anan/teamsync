package com.teamsync.projectmanagementservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDeletedEvent {
    private Long projectId;
    private String projectTitle;
    private Long deletedBy;
    private String deletedAt;
}

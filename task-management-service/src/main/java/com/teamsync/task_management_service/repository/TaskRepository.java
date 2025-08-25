package com.teamsync.task_management_service.repository;

import com.teamsync.task_management_service.entity.Tasks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Tasks, Long> {

    @Query("SELECT t FROM Tasks t LEFT JOIN FETCH t.parentTask WHERE t.id = :id")
    Optional<Tasks> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT t FROM Tasks t WHERE t.parentTask.id = :parentTaskId")
    List<Tasks> findSubtasksByParentTaskId(@Param("parentTaskId") Long parentTaskId);

    // Method for getting tasks by project ID
    @Query("SELECT t FROM Tasks t LEFT JOIN FETCH t.parentTask WHERE t.project = :projectId")
    List<Tasks> findByProjectIdWithDetails(@Param("projectId") Long projectId);

    // Method for getting tasks by project ID and status (useful for kanban)
    @Query("SELECT t FROM Tasks t LEFT JOIN FETCH t.parentTask WHERE t.project = :projectId AND t.status = :status")
    List<Tasks> findByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") Tasks.TaskStatus status);

    @Query("SELECT t FROM Tasks t WHERE t.assignedBy = :userId OR t.assignedTo = :userId ORDER BY t.assignedAt DESC")
    List<Tasks> findUserInvolvedTasks(@Param("userId") Long userId);

    @Query("SELECT t FROM Tasks t WHERE t.assignedTo = :userId ORDER BY t.assignedAt DESC")
    List<Tasks> findTasksAssignedToUser(@Param("userId") Long userId);

    // Method for deleting all tasks by project ID (for cascade deletion)
    @Modifying
    @Query("DELETE FROM Tasks t WHERE t.project = :projectId")
    int deleteByProjectId(@Param("projectId") Long projectId);
}
package com.teamsync.task_management_service.repository;

import com.teamsync.task_management_service.entity.TaskStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskStatusHistoryRepository extends JpaRepository<TaskStatusHistory, Long> {

    @Query("SELECT tsh FROM TaskStatusHistory tsh WHERE tsh.task.id = :taskId ORDER BY tsh.changedAt DESC")
    List<TaskStatusHistory> findByTaskIdOrderByChangedAtDesc(@Param("taskId") Long taskId);
}
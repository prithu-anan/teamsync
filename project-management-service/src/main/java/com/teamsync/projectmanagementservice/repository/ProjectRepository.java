package com.teamsync.projectmanagementservice.repository;

import com.teamsync.projectmanagementservice.entity.Projects;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Projects, Long> {
    boolean existsByCreatedBy(Long createdBy);
}

package com.teamsync.projectmanagementservice.repository;

import com.teamsync.projectmanagementservice.entity.ProjectMemberId;
import com.teamsync.projectmanagementservice.entity.ProjectMembers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMembers, ProjectMemberId> {

    @Modifying
    @Query("DELETE FROM ProjectMembers pm WHERE pm.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);

    // Find methods - Fixed to use 'user' instead of 'user.id'
    List<ProjectMembers> findByProjectId(Long projectId);

    List<ProjectMembers> findByUser(Long userId);

    @Query("SELECT pm FROM ProjectMembers pm WHERE pm.project.id = :projectId AND pm.user = :userId")
    Optional<ProjectMembers> findByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);

    // Existence check method
    boolean existsByProjectIdAndUser(Long projectId, Long userId);

    // Alternative using @Query if the above doesn't work
    @Query("SELECT COUNT(pm) > 0 FROM ProjectMembers pm WHERE pm.project.id = :projectId AND pm.user = :userId")
    boolean existsByProjectIdAndUserIdCustom(@Param("projectId") Long projectId, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM ProjectMembers pm WHERE pm.user = :userId")
    void deleteByUserId(@Param("userId") Long userId);

}
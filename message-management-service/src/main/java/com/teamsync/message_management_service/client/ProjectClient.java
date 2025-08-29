package com.teamsync.message_management_service.client;

import com.teamsync.message_management_service.dto.ProjectDTO;
import com.teamsync.message_management_service.dto.ProjectMemberDTO;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class ProjectClient {
    
    // Dummy data storage
    private static final Map<Long, ProjectDTO> projects = new HashMap<>();
    
    static {
        // Initialize dummy projects
        projects.put(1L, ProjectDTO.builder()
                .id(1L)
                .title("TeamSync Communication Platform")
                .description("A comprehensive team communication and project management platform with real-time messaging, file sharing, and collaboration tools.")
                .createdBy(1L)
                .createdAt(ZonedDateTime.now().minusMonths(6))
                .members(Arrays.asList(
                    ProjectMemberDTO.builder()
                            .userId(1L)
                            .role("PROJECT_MANAGER")
                            .joinedAt(ZonedDateTime.now().minusMonths(6))
                            .build(),
                    ProjectMemberDTO.builder()
                            .userId(2L)
                            .role("DEVELOPER")
                            .joinedAt(ZonedDateTime.now().minusMonths(5))
                            .build(),
                    ProjectMemberDTO.builder()
                            .userId(3L)
                            .role("DEVELOPER")
                            .joinedAt(ZonedDateTime.now().minusMonths(4))
                            .build(),
                    ProjectMemberDTO.builder()
                            .userId(4L)
                            .role("DESIGNER")
                            .joinedAt(ZonedDateTime.now().minusMonths(4))
                            .build()
                ))
                .build());
                
        projects.put(2L, ProjectDTO.builder()
                .id(2L)
                .title("E-Commerce Mobile App")
                .description("Cross-platform mobile application for online shopping with features like product catalog, shopping cart, payment integration, and order tracking.")
                .createdBy(2L)
                .createdAt(ZonedDateTime.now().minusMonths(4))
                .members(Arrays.asList(
                    ProjectMemberDTO.builder()
                            .userId(2L)
                            .role("PROJECT_MANAGER")
                            .joinedAt(ZonedDateTime.now().minusMonths(4))
                            .build(),
                    ProjectMemberDTO.builder()
                            .userId(5L)
                            .role("DEVELOPER")
                            .joinedAt(ZonedDateTime.now().minusMonths(3))
                            .build(),
                    ProjectMemberDTO.builder()
                            .userId(6L)
                            .role("QA_ENGINEER")
                            .joinedAt(ZonedDateTime.now().minusMonths(3))
                            .build(),
                    ProjectMemberDTO.builder()
                            .userId(7L)
                            .role("DEVELOPER")
                            .joinedAt(ZonedDateTime.now().minusMonths(2))
                            .build()
                ))
                .build());
                
        projects.put(3L, ProjectDTO.builder()
                .id(3L)
                .title("Data Analytics Dashboard")
                .description("Business intelligence dashboard for real-time data visualization, reporting, and analytics with interactive charts and automated insights.")
                .createdBy(8L)
                .createdAt(ZonedDateTime.now().minusMonths(3))
                .members(Arrays.asList(
                    ProjectMemberDTO.builder()
                            .userId(8L)
                            .role("DATA_ANALYST")
                            .joinedAt(ZonedDateTime.now().minusMonths(3))
                            .build(),
                    ProjectMemberDTO.builder()
                            .userId(1L)
                            .role("DEVELOPER")
                            .joinedAt(ZonedDateTime.now().minusMonths(2))
                            .build(),
                    ProjectMemberDTO.builder()
                            .userId(4L)
                            .role("DESIGNER")
                            .joinedAt(ZonedDateTime.now().minusMonths(2))
                            .build()
                ))
                .build());
                
        projects.put(4L, ProjectDTO.builder()
                .id(4L)
                .title("Cloud Infrastructure Migration")
                .description("Migration of legacy systems to cloud infrastructure with containerization, microservices architecture, and automated deployment pipelines.")
                .createdBy(3L)
                .createdAt(ZonedDateTime.now().minusMonths(5))
                .members(Arrays.asList(
                    ProjectMemberDTO.builder()
                            .userId(3L)
                            .role("DEVOPS_ENGINEER")
                            .joinedAt(ZonedDateTime.now().minusMonths(5))
                            .build(),
                    ProjectMemberDTO.builder()
                            .userId(9L)
                            .role("SYSTEM_ADMIN")
                            .joinedAt(ZonedDateTime.now().minusMonths(4))
                            .build(),
                    ProjectMemberDTO.builder()
                            .userId(5L)
                            .role("DEVELOPER")
                            .joinedAt(ZonedDateTime.now().minusMonths(3))
                            .build()
                ))
                .build());
                
        projects.put(5L, ProjectDTO.builder()
                .id(5L)
                .title("AI-Powered Customer Support")
                .description("Intelligent customer support system with chatbots, sentiment analysis, ticket routing, and automated response generation.")
                .createdBy(10L)
                .createdAt(ZonedDateTime.now().minusMonths(2))
                .members(Arrays.asList(
                    ProjectMemberDTO.builder()
                            .userId(10L)
                            .role("PRODUCT_OWNER")
                            .joinedAt(ZonedDateTime.now().minusMonths(2))
                            .build(),
                    ProjectMemberDTO.builder()
                            .userId(1L)
                            .role("DEVELOPER")
                            .joinedAt(ZonedDateTime.now().minusMonths(1))
                            .build(),
                    ProjectMemberDTO.builder()
                            .userId(8L)
                            .role("DATA_ANALYST")
                            .joinedAt(ZonedDateTime.now().minusMonths(1))
                            .build(),
                    ProjectMemberDTO.builder()
                            .userId(6L)
                            .role("QA_ENGINEER")
                            .joinedAt(ZonedDateTime.now().minusWeeks(3))
                            .build()
                ))
                .build());
                
        projects.put(6L, ProjectDTO.builder()
                .id(6L)
                .title("Security Audit Platform")
                .description("Comprehensive security audit and vulnerability assessment platform with automated scanning, compliance reporting, and remediation tracking.")
                .createdBy(9L)
                .createdAt(ZonedDateTime.now().minusMonths(1))
                .members(Arrays.asList(
                    ProjectMemberDTO.builder()
                            .userId(9L)
                            .role("SECURITY_ENGINEER")
                            .joinedAt(ZonedDateTime.now().minusMonths(1))
                            .build(),
                    ProjectMemberDTO.builder()
                            .userId(3L)
                            .role("DEVOPS_ENGINEER")
                            .joinedAt(ZonedDateTime.now().minusWeeks(3))
                            .build(),
                    ProjectMemberDTO.builder()
                            .userId(5L)
                            .role("DEVELOPER")
                            .joinedAt(ZonedDateTime.now().minusWeeks(2))
                            .build()
                ))
                .build());
                
        projects.put(7L, ProjectDTO.builder()
                .id(7L)
                .title("Learning Management System")
                .description("Online learning platform with course management, progress tracking, interactive content, assessments, and certification system.")
                .createdBy(4L)
                .createdAt(ZonedDateTime.now().minusWeeks(6))
                .members(Arrays.asList(
                    ProjectMemberDTO.builder()
                            .userId(4L)
                            .role("UX_DESIGNER")
                            .joinedAt(ZonedDateTime.now().minusWeeks(6))
                            .build(),
                    ProjectMemberDTO.builder()
                            .userId(7L)
                            .role("FRONTEND_DEVELOPER")
                            .joinedAt(ZonedDateTime.now().minusWeeks(5))
                            .build(),
                    ProjectMemberDTO.builder()
                            .userId(5L)
                            .role("BACKEND_DEVELOPER")
                            .joinedAt(ZonedDateTime.now().minusWeeks(4))
                            .build(),
                    ProjectMemberDTO.builder()
                            .userId(6L)
                            .role("QA_ENGINEER")
                            .joinedAt(ZonedDateTime.now().minusWeeks(3))
                            .build()
                ))
                .build());
    }
    
    // Method used in ChannelService.createChannel() and ChannelService.updateChannel()
    public Optional<ProjectDTO> findById(Long id) {
        return Optional.ofNullable(projects.get(id));
    }
}
package com.teamsync.task_management_service.mapper;

import com.teamsync.task_management_service.dto.TaskStatusHistoryDTO;
import com.teamsync.task_management_service.entity.TaskStatusHistory;
import com.teamsync.task_management_service.entity.Tasks;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskStatusHistoryMapper {

    @Mapping(source = "status", target = "status", qualifiedByName = "taskStatusToString")
    @Mapping(source = "changedBy", target = "changedBy")
    TaskStatusHistoryDTO toDto(TaskStatusHistory entity);

    List<TaskStatusHistoryDTO> toDtoList(List<TaskStatusHistory> entities);

    @Named("taskStatusToString")
    default String taskStatusToString(Tasks.TaskStatus status) {
        return status != null ? status.name() : null;
    }
}

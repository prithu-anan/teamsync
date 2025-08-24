package com.teamsync.task_management_service.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuccessResponse<T> {
    private int code;
    private HttpStatus status;
    private String message;
    private T data;
    private Map<String, Object> metadata;
}
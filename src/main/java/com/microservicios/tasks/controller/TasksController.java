package com.microservicios.tasks.controller;

import java.util.List;

import com.microservicios.tasks.dto.ApiResponse;
import com.microservicios.tasks.dto.TasksResponse;
import com.microservicios.tasks.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.microservicios.tasks.dto.TaskListDto;
import com.microservicios.tasks.service.TasksService;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TasksController {

    private final TasksService tasksService;

    @GetMapping
    public ResponseEntity<ApiResponse<TasksResponse>> getTaskLists(
            @RequestHeader("Authorization") String authHeader) {

        log.info("Received request to fetch task lists");
        String accessToken = extractAccessToken(authHeader);

        List<TaskListDto> taskLists = tasksService.listTasks(accessToken);
        TasksResponse response = TasksResponse.from(taskLists);

        return ResponseEntity.ok(ApiResponse.success("Listas de tareas obtenidas exitosamente", response));
    }

    @PostMapping("/{taskListId}/tasks/{taskId}/complete")
    public ResponseEntity<ApiResponse<Void>> markTaskComplete(
            @PathVariable String taskListId,
            @PathVariable String taskId,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Received request to mark task {} as completed in list {}", taskId, taskListId);
        String accessToken = extractAccessToken(authHeader);

        tasksService.markTaskComplete(taskListId, taskId, accessToken);

        return ResponseEntity.ok(ApiResponse.success("Tarea marcada como completada exitosamente", null));
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<ServiceInfo>> getServiceInfo() {
        ServiceInfo info = ServiceInfo.builder()
                .version("1.0.0")
                .description("Microservicio para Google Tasks API")
                .endpoints(new String[]{
                        "GET /api/tasks - Listar todas las listas de tareas",
                        "POST /api/tasks/{taskListId}/tasks/{taskId}/complete - Marcar tarea como completada",
                        "GET /api/tasks/info - Información del servicio"
                })
                .usage("Los endpoints requieren el header 'Authorization: Bearer {google_access_token}'")
                .note("Microservicio para lectura y modificación de Google Tasks")
                .build();

        return ResponseEntity.ok(ApiResponse.success(info));
    }

    private String extractAccessToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("El header de autorización está ausente o es inválido");
        }
        return authHeader.substring(7);
    }

    @lombok.Data
    @lombok.Builder
    private static class ServiceInfo {
        private String version;
        private String description;
        private String[] endpoints;
        private String usage;
        private String note;
    }
}

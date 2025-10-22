package com.microservicios.tasks.controller;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.microservicios.tasks.dto.TaskListDto;
import com.microservicios.tasks.service.TasksService;

@RestController
@RequestMapping("/api/tasks")
public class TasksController {

    private static final Logger logger = Logger.getLogger(TasksController.class.getName());

    @Autowired
    private TasksService tasksService;

    @GetMapping
    public ResponseEntity<Object> getTaskLists(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new Object() {
                            public final boolean success = false;
                            public final String message = "Autorización requerida. Header 'Authorization' no encontrado o inválido.";
                        });
            }

            String googleAccessToken = authHeader.substring(7); // Remove "Bearer "
            logger.info("Obteniendo listas de tareas de Google Tasks");

            List<TaskListDto> taskLists = tasksService.listTasks(googleAccessToken);

            final List<TaskListDto> finalTaskLists = taskLists;
            final int listCount = taskLists.size();

            // Count total tasks
            int totalTasks = 0;
            for (TaskListDto taskList : taskLists) {
                if (taskList.getTasks() != null) {
                    totalTasks += taskList.getTasks().size();
                }
            }
            final int taskCount = totalTasks;

            Object response = new Object() {
                public final boolean success = true;
                public final String message = "Listas de tareas obtenidas exitosamente";
                public final int taskListCount = listCount;
                public final int totalTasks = taskCount;
                public final List<TaskListDto> taskLists = finalTaskLists;
                public final long timestamp = System.currentTimeMillis();
                public final String note = "Mostrando " + listCount + " listas con " + taskCount + " tareas total";
            };

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            logger.severe("Error al obtener tareas: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Object() {
                        public final boolean success = false;
                        public final String message = "Error interno del servidor: " + e.getMessage();
                    });
        }
    }

    @PostMapping("/{taskId}/complete")
    public ResponseEntity<Object> markTaskComplete(
            @PathVariable String taskId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new Object() {
                            public final boolean success = false;
                            public final String message = "Autorización requerida. Header 'Authorization' no encontrado o inválido.";
                        });
            }

            String googleAccessToken = authHeader.substring(7); // Remove "Bearer "
            logger.info("Marcando tarea como completada: " + taskId);

            boolean success = tasksService.markTaskComplete(taskId, googleAccessToken);

            if (success) {
                final String completedTaskId = taskId;
                return ResponseEntity.ok().body(new Object() {
                    public final boolean success = true;
                    public final String message = "Tarea marcada como completada exitosamente";
                    public final String taskId = completedTaskId;
                    public final long timestamp = System.currentTimeMillis();
                });
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Object() {
                            public final boolean success = false;
                            public final String message = "No se pudo encontrar o completar la tarea con ID: " + taskId;
                        });
            }

        } catch (Exception e) {
            logger.severe("Error al marcar tarea como completada: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Object() {
                        public final boolean success = false;
                        public final String message = "Error interno del servidor: " + e.getMessage();
                    });
        }
    }

    @GetMapping("/info")
    public ResponseEntity<Object> getTasksInfo() {
        return ResponseEntity.ok(new Object() {
            public final String version = "1.0.0";
            public final String description = "Microservicio para Google Tasks API";
            public final String[] endpoints = {
                "GET /api/tasks - Listar todas las listas de tareas",
                "POST /api/tasks/{taskId}/complete - Marcar tarea como completada",
                "GET /api/tasks/info - Información del microservicio"
            };
            public final String usage = "Endpoints requieren header 'Authorization: Bearer {google_access_token}'";
            public final String note = "Microservicio para lectura y modificación de Google Tasks";
        });
    }
}
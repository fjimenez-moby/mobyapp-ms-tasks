package com.microservicios.tasks.service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import com.microservicios.tasks.enums.TaskStatus;
import com.microservicios.tasks.exception.GoogleApiException;
import com.microservicios.tasks.exception.TaskNotFoundException;
import com.microservicios.tasks.factory.GoogleTasksClientFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.microservicios.tasks.dto.TaskDto;
import com.microservicios.tasks.dto.TaskListDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class TasksService {

    private final GoogleTasksClientFactory clientFactory;

    @Value("${google.tasks.max-results:50}")
    private int maxResults;

    public List<TaskListDto> listTasks(String accessToken) {
        try {
            log.info("Obteniendo listas de tareas desde Google Tasks API");

            com.google.api.services.tasks.Tasks tasksClient = clientFactory.createClient(accessToken);
            com.google.api.services.tasks.model.TaskLists taskLists = tasksClient.tasklists().list().execute();

            if (taskLists.getItems() == null || taskLists.getItems().isEmpty()) {
                log.info("No se encontraron listas de tareas");
                return new ArrayList<>();
            }

            List<TaskListDto> taskListDtos = new ArrayList<>();
            for (com.google.api.services.tasks.model.TaskList taskList : taskLists.getItems()) {
                TaskListDto taskListDto = buildTaskListDto(tasksClient, taskList);
                taskListDtos.add(taskListDto);
            }

            log.info("Se obtuvieron exitosamente {} listas de tareas", taskListDtos.size());
            return taskListDtos;

        } catch (IOException e) {
            log.error("Error al obtener tareas desde Google API: {}", e.getMessage(), e);
            throw new GoogleApiException("Error al obtener tareas desde Google API", e);
        }
    }

    private TaskListDto buildTaskListDto(com.google.api.services.tasks.Tasks tasksClient,
                                         com.google.api.services.tasks.model.TaskList taskList) throws IOException {
        String listId = taskList.getId();
        String listTitle = taskList.getTitle() != null ? taskList.getTitle() : "Untitled";

        List<TaskDto> tasks = fetchTasksForList(tasksClient, listId);

        return TaskListDto.builder()
                .id(listId)
                .title(listTitle)
                .tasks(tasks)
                .build();
    }

    private List<TaskDto> fetchTasksForList(com.google.api.services.tasks.Tasks tasksClient,
                                            String listId) throws IOException {
        com.google.api.services.tasks.model.Tasks tasks = tasksClient.tasks()
                .list(listId)
                .setMaxResults(Integer.valueOf(maxResults))
                .execute();

        if (tasks.getItems() == null) {
            return new ArrayList<>();
        }

        List<TaskDto> taskDtos = new ArrayList<>();
        for (com.google.api.services.tasks.model.Task task : tasks.getItems()) {
            TaskDto taskDto = convertToTaskDto(task);
            taskDtos.add(taskDto);
        }

        return taskDtos;
    }

    private TaskDto convertToTaskDto(com.google.api.services.tasks.model.Task task) {
        String taskId = task.getId() != null ? task.getId() : "";
        String title = task.getTitle() != null ? task.getTitle() : "Untitled";
        TaskStatus status = TaskStatus.fromValue(task.getStatus() != null ? task.getStatus() : "needsAction");

        LocalDateTime dueDate = parseDueDate(taskId, task.getDue());
        String notes = task.getNotes() != null ? task.getNotes() : "";

        return TaskDto.builder()
                .id(taskId)
                .title(title)
                .status(status)
                .dueDate(dueDate)
                .notes(notes)
                .build();
    }

    private LocalDateTime parseDueDate(String taskId, String dueDateString) {
        if (dueDateString == null || dueDateString.trim().isEmpty()) {
            return null;
        }

        try {
            // Intentar parsear formato RFC 3339 (ej: "2021-07-09T00:00:00.000Z")
            Instant instant = Instant.parse(dueDateString);
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        } catch (DateTimeParseException e) {
            try {
                // Intentar formato alternativo solo con fecha (ej: "2021-07-09")
                return LocalDateTime.parse(dueDateString + "T00:00:00",
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException ex) {
                log.warn("No se pudo parsear la fecha de vencimiento '{}' para la tarea {}: {}",
                        dueDateString, taskId, ex.getMessage());
                return null;
            }
        }
    }

    public void markTaskComplete(String taskListId, String taskId, String accessToken) {
        try {
            log.info("Marcando tarea {} como completada en la lista {}", taskId, taskListId);

            com.google.api.services.tasks.Tasks tasksClient = clientFactory.createClient(accessToken);

            // Obtener la tarea primero para verificar que existe
            com.google.api.services.tasks.model.Task task = tasksClient.tasks()
                    .get(taskListId, taskId)
                    .execute();

            if (task == null) {
                throw new TaskNotFoundException(taskId);
            }

            // Actualizar el estado de la tarea a completada
            task.setStatus("completed");
            tasksClient.tasks()
                    .update(taskListId, taskId, task)
                    .execute();

            log.info("Tarea {} marcada exitosamente como completada", taskId);

        } catch (IOException e) {
            log.error("Error al marcar tarea {} como completada: {}", taskId, e.getMessage(), e);
            throw new GoogleApiException("Error al marcar tarea como completada", e);
        }
    }
}
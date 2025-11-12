package com.microservicios.tasks.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
            log.info("Fetching task lists from Google Tasks API");

            com.google.api.services.tasks.Tasks tasksClient = clientFactory.createClient(accessToken);
            com.google.api.services.tasks.model.TaskLists taskLists = tasksClient.tasklists().list().execute();

            if (taskLists.getItems() == null || taskLists.getItems().isEmpty()) {
                log.info("No task lists found");
                return new ArrayList<>();
            }

            List<TaskListDto> taskListDtos = new ArrayList<>();
            for (com.google.api.services.tasks.model.TaskList taskList : taskLists.getItems()) {
                TaskListDto taskListDto = buildTaskListDto(tasksClient, taskList);
                taskListDtos.add(taskListDto);
            }

            log.info("Successfully fetched {} task lists with total tasks", taskListDtos.size());
            return taskListDtos;

        } catch (IOException e) {
            log.error("Error fetching tasks from Google API: {}", e.getMessage(), e);
            throw new GoogleApiException("Failed to fetch tasks from Google API", e);
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
                .setMaxResults((long) maxResults)
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
        LocalDateTime dueDate = task.getDue() != null ?
                LocalDateTime.ofInstant(task.getDue().toInstant(), ZoneId.systemDefault()) : null;
        String notes = task.getNotes() != null ? task.getNotes() : "";

        return TaskDto.builder()
                .id(taskId)
                .title(title)
                .status(status)
                .dueDate(dueDate)
                .notes(notes)
                .build();
    }

}
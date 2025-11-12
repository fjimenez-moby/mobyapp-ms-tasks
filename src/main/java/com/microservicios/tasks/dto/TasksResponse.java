package com.microservicios.tasks.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TasksResponse {
    private List<TaskListDto> taskLists;
    private int taskListCount;
    private int totalTasks;
    private String note;

    public static TasksResponse from(List<TaskListDto> taskLists) {
        int totalTasks = taskLists.stream()
                .mapToInt(taskList -> taskList.getTasks() != null ? taskList.getTasks().size() : 0)
                .sum();

        return TasksResponse.builder()
                .taskLists(taskLists)
                .taskListCount(taskLists.size())
                .totalTasks(totalTasks)
                .note(String.format("Showing %d lists with %d total tasks", taskLists.size(), totalTasks))
                .build();
    }
}

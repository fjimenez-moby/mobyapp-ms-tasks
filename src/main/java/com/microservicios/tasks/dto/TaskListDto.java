package com.microservicios.tasks.dto;

import java.util.List;

public class TaskListDto {
    private String id;
    private String title;
    private String type; // "tasklist"
    private List<TaskDto> tasks;

    public TaskListDto() {}

    public TaskListDto(String id, String title, String type) {
        this.id = id;
        this.title = title;
        this.type = type;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<TaskDto> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskDto> tasks) {
        this.tasks = tasks;
    }
}
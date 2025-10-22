package com.microservicios.tasks.dto;

public class TaskDto {
    private String id;
    private String title;
    private String status;
    private String dueDate;
    private String notes;
    private String type; // "task"

    public TaskDto() {}

    public TaskDto(String id, String title, String status, String dueDate, String notes, String type) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.dueDate = dueDate;
        this.notes = notes;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
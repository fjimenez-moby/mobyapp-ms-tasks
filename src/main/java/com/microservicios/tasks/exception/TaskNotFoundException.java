package com.microservicios.tasks.exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String taskId) {
        super("Tarea no encontrada con ID: " + taskId);
    }

    public TaskNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
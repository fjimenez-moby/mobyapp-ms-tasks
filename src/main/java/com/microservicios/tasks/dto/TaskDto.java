package com.microservicios.tasks.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.microservicios.tasks.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    private String id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Status is required")
    private TaskStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dueDate;

    private String notes;
}
package com.microservicios.tasks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskListDto {
    private String id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Tasks list cannot be null")
    @Builder.Default
    private List<TaskDto> tasks = new ArrayList<>();
}
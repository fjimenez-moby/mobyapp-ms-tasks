package com.microservicios.tasks.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private boolean success;
    private String message;
    private String error;
    private int status;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static ErrorResponse of(String message, String error, int status) {
        return ErrorResponse.builder()
                .success(false)
                .message(message)
                .error(error)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

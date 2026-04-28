package com.taskmanager.app.dto;

import com.taskmanager.app.model.Priority;
import com.taskmanager.app.model.Status;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private LocalDate dueDate;

    private Priority priority;

    private Status status;
}

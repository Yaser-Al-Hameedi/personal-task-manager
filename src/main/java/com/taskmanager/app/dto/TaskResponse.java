package com.taskmanager.app.dto;

import com.taskmanager.app.model.Priority;
import com.taskmanager.app.model.Status;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private Priority priority;
    private Status status;
}

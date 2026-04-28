package com.taskmanager.app.dto;

import com.taskmanager.app.model.Priority;
import com.taskmanager.app.model.Status;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskSuggestion {
    private String title;
    private String description;
    private LocalDate dueDate;
    private Priority priority;
    private Status status;
}

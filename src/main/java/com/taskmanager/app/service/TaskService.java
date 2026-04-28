package com.taskmanager.app.service;

import com.taskmanager.app.dto.TaskRequest;
import com.taskmanager.app.dto.TaskResponse;
import com.taskmanager.app.exception.TaskNotFoundException;
import com.taskmanager.app.model.Priority;
import com.taskmanager.app.model.Status;
import com.taskmanager.app.model.Task;
import com.taskmanager.app.repository.TaskRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskResponse createTask(TaskRequest request) {
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .status(request.getStatus() != null ? request.getStatus() : Status.TODO)
                .build();
        @NonNull Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public TaskResponse getTaskById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = findOrThrow(id);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority() != null ? request.getPriority() : task.getPriority());
        task.setStatus(request.getStatus() != null ? request.getStatus() : task.getStatus());
        return toResponse(taskRepository.save(task));
    }

    public void deleteTask(Long id) {
        findOrThrow(id);
        taskRepository.deleteById(Objects.requireNonNull(id));
    }

    private Task findOrThrow(Long id) {
        return taskRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    private TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .dueDate(task.getDueDate())
                .priority(task.getPriority())
                .status(task.getStatus())
                .build();
    }
}

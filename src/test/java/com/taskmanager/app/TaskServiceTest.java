package com.taskmanager.app;

import com.taskmanager.app.dto.TaskRequest;
import com.taskmanager.app.dto.TaskResponse;
import com.taskmanager.app.exception.TaskNotFoundException;
import com.taskmanager.app.model.Priority;
import com.taskmanager.app.model.Status;
import com.taskmanager.app.model.Task;
import com.taskmanager.app.repository.TaskRepository;
import com.taskmanager.app.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task sampleTask;
    private TaskRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleTask = Task.builder()
                .id(1L)
                .title("Write tests")
                .description("Cover the service layer")
                .dueDate(LocalDate.of(2026, 5, 1))
                .priority(Priority.HIGH)
                .status(Status.TODO)
                .build();

        sampleRequest = new TaskRequest();
        sampleRequest.setTitle("Write tests");
        sampleRequest.setDescription("Cover the service layer");
        sampleRequest.setDueDate(LocalDate.of(2026, 5, 1));
        sampleRequest.setPriority(Priority.HIGH);
        sampleRequest.setStatus(Status.TODO);
    }

    @Test
    void createTask_returnsCreatedTask() {
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        TaskResponse response = taskService.createTask(sampleRequest);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Write tests");
        assertThat(response.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(response.getStatus()).isEqualTo(Status.TODO);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createTask_appliesDefaultsWhenPriorityAndStatusOmitted() {
        sampleRequest.setPriority(null);
        sampleRequest.setStatus(null);

        Task savedTask = Task.builder()
                .id(2L)
                .title("Write tests")
                .priority(Priority.MEDIUM)
                .status(Status.TODO)
                .build();
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        TaskResponse response = taskService.createTask(sampleRequest);

        assertThat(response.getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(response.getStatus()).isEqualTo(Status.TODO);
    }

    @Test
    void getAllTasks_returnsAllTasks() {
        when(taskRepository.findAll()).thenReturn(List.of(sampleTask));

        List<TaskResponse> responses = taskService.getAllTasks();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTitle()).isEqualTo("Write tests");
    }

    @Test
    void getTaskById_returnsTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        TaskResponse response = taskService.getTaskById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Write tests");
    }

    @Test
    void getTaskById_throwsWhenNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(99L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateTask_updatesAndReturnsTask() {
        sampleRequest.setTitle("Updated title");
        sampleRequest.setPriority(Priority.LOW);

        Task updatedTask = Task.builder()
                .id(1L)
                .title("Updated title")
                .priority(Priority.LOW)
                .status(Status.TODO)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        TaskResponse response = taskService.updateTask(1L, sampleRequest);

        assertThat(response.getTitle()).isEqualTo("Updated title");
        assertThat(response.getPriority()).isEqualTo(Priority.LOW);
    }

    @Test
    void updateTask_throwsWhenNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask(99L, sampleRequest))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteTask_deletesSuccessfully() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        taskService.deleteTask(1L);

        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteTask_throwsWhenNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(99L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("99");
    }
}

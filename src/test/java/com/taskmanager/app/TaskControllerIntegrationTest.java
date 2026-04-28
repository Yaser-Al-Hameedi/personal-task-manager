package com.taskmanager.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.app.dto.SuggestRequest;
import com.taskmanager.app.dto.TaskRequest;
import com.taskmanager.app.dto.TaskResponse;
import com.taskmanager.app.dto.TaskSuggestion;
import com.taskmanager.app.exception.TaskNotFoundException;
import com.taskmanager.app.model.Priority;
import com.taskmanager.app.model.Status;
import com.taskmanager.app.service.OpenAiService;
import com.taskmanager.app.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private OpenAiService openAiService;

    private TaskResponse sampleResponse() {
        return TaskResponse.builder()
                .id(1L)
                .title("Write tests")
                .description("Cover the service layer")
                .dueDate(LocalDate.of(2026, 5, 1))
                .priority(Priority.HIGH)
                .status(Status.TODO)
                .build();
    }

    @Test
    void createTask_returns201WithBody() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle("Write tests");
        request.setPriority(Priority.HIGH);
        request.setStatus(Status.TODO);

        when(taskService.createTask(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Write tests"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void createTask_missingTitle_returns400WithMessage() throws Exception {
        TaskRequest request = new TaskRequest();
        // title intentionally omitted

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getAllTasks_returns200WithList() throws Exception {
        when(taskService.getAllTasks()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Write tests"));
    }

    @Test
    void getTaskById_returns200WithBody() throws Exception {
        when(taskService.getTaskById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Write tests"));
    }

    @Test
    void getTaskById_unknownId_returns404WithMessage() throws Exception {
        when(taskService.getTaskById(99L)).thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(get("/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found with id: 99"));
    }

    @Test
    void updateTask_returns200WithUpdatedBody() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle("Updated title");
        request.setPriority(Priority.LOW);
        request.setStatus(Status.IN_PROGRESS);

        TaskResponse updated = TaskResponse.builder()
                .id(1L)
                .title("Updated title")
                .priority(Priority.LOW)
                .status(Status.IN_PROGRESS)
                .build();

        when(taskService.updateTask(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void deleteTask_returns204() throws Exception {
        doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTask_unknownId_returns404WithMessage() throws Exception {
        doThrow(new TaskNotFoundException(99L)).when(taskService).deleteTask(99L);

        mockMvc.perform(delete("/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found with id: 99"));
    }

    @Test
    void suggestTask_returns200WithSuggestion() throws Exception {
        SuggestRequest request = new SuggestRequest();
        request.setDescription("remind me to submit the quarterly report before Friday");

        TaskSuggestion suggestion = new TaskSuggestion();
        suggestion.setTitle("Submit quarterly report");
        suggestion.setPriority(Priority.HIGH);
        suggestion.setStatus(Status.TODO);

        when(openAiService.suggest(any())).thenReturn(suggestion);

        mockMvc.perform(post("/tasks/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Submit quarterly report"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }
}

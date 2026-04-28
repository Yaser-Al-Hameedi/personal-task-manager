package com.taskmanager.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskmanager.app.dto.TaskSuggestion;
import com.taskmanager.app.model.Priority;
import com.taskmanager.app.model.Status;
import com.taskmanager.app.service.OpenAiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SuppressWarnings("null")
class OpenAiServiceTest {

    private MockRestServiceServer mockServer;
    private OpenAiService openAiService;

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);

        // Match Spring Boot's auto-configured ObjectMapper: ISO dates, JSR310 module
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        openAiService = new OpenAiService(restTemplate, objectMapper);
        ReflectionTestUtils.setField(openAiService, "apiKey", "test-key");
        ReflectionTestUtils.setField(openAiService, "apiUrl", API_URL);
        ReflectionTestUtils.setField(openAiService, "model", "gpt-4o-mini");
    }

    @Test
    void suggest_parsesOpenAiResponseIntoTaskSuggestion() {
        // The inner content field is itself an escaped JSON string — this mirrors
        // the real OpenAI response shape when JSON mode is active
        String openAiResponse = """
                {
                  "choices": [{
                    "message": {
                      "content": "{\\"title\\":\\"Submit quarterly report\\",\\"description\\":\\"Ensure the report is completed and submitted\\",\\"dueDate\\":\\"2026-05-01\\",\\"priority\\":\\"HIGH\\",\\"status\\":\\"TODO\\"}"
                    }
                  }]
                }
                """;

        mockServer.expect(requestTo(API_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andRespond(withSuccess(openAiResponse, MediaType.APPLICATION_JSON));

        TaskSuggestion result = openAiService.suggest("remind me to submit the quarterly report before Friday");

        assertThat(result.getTitle()).isEqualTo("Submit quarterly report");
        assertThat(result.getDescription()).isEqualTo("Ensure the report is completed and submitted");
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(result.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(result.getStatus()).isEqualTo(Status.TODO);

        mockServer.verify();
    }
}

package com.taskmanager.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SuggestRequest {

    @NotBlank(message = "Description is required")
    private String description;
}

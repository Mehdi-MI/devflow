package com.mehdi.devflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBuildTaskRequest {

    @NotBlank(message = "Task name is required")
    private String name;

    @NotBlank(message = "Repository URL is required")
    private String repositoryUrl;

    @NotBlank(message = "Branch is required")
    private String branch;
}

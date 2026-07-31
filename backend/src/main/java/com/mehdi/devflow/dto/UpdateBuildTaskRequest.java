package com.mehdi.devflow.dto;

import com.mehdi.devflow.enums.BuildStatus;
import lombok.Data;

@Data
public class UpdateBuildTaskRequest {

    private String name;

    private String repositoryUrl;

    private String branch;

    private BuildStatus status;

    private String logs;
}

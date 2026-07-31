package com.mehdi.devflow.dto;

import com.mehdi.devflow.enums.BuildStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class BuildTaskResponse {

    private UUID id;

    private String name;

    private String repositoryUrl;

    private String branch;

    private BuildStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private String logs;
}

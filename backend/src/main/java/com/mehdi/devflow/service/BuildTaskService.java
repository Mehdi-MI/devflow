package com.mehdi.devflow.service;

import com.mehdi.devflow.dto.BuildTaskResponse;
import com.mehdi.devflow.dto.CreateBuildTaskRequest;
import com.mehdi.devflow.dto.UpdateBuildTaskRequest;

import java.util.List;
import java.util.UUID;

public interface BuildTaskService {

    BuildTaskResponse createTask(CreateBuildTaskRequest request);

    List<BuildTaskResponse> getAllTasks();

    BuildTaskResponse getTaskById(UUID id);

    BuildTaskResponse updateTask(UUID id, UpdateBuildTaskRequest request);

    void deleteTask(UUID id);
}

package com.mehdi.devflow.service.impl;

import com.mehdi.devflow.dto.BuildTaskResponse;
import com.mehdi.devflow.dto.CreateBuildTaskRequest;
import com.mehdi.devflow.dto.UpdateBuildTaskRequest;
import com.mehdi.devflow.enums.BuildStatus;
import com.mehdi.devflow.entity.BuildTask;
import com.mehdi.devflow.exception.ResourceNotFoundException;
import com.mehdi.devflow.mapper.BuildTaskMapper;
import com.mehdi.devflow.repository.BuildTaskRepository;
import com.mehdi.devflow.service.BuildTaskService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BuildTaskServiceImpl implements BuildTaskService {

    private static final Logger log = LoggerFactory.getLogger(BuildTaskServiceImpl.class);

    private final BuildTaskRepository repository;
    private final BuildTaskMapper mapper;

    @Override
    public BuildTaskResponse createTask(CreateBuildTaskRequest request) {

        BuildTask task = mapper.toEntity(request);

        BuildTask savedTask = repository.save(task);

        log.info("Created build task with id={}", savedTask.getId());

        return mapper.toResponse(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuildTaskResponse> getAllTasks() {

        log.debug("Retrieving all build tasks");

        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BuildTaskResponse getTaskById(UUID id) {

        BuildTask task = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Build task not found with id={}", id);
                    return new ResourceNotFoundException("Build task not found: " + id);
                });

        return mapper.toResponse(task);
    }

    @Override
    public BuildTaskResponse updateTask(UUID id, UpdateBuildTaskRequest request) {

        BuildTask task = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Build task not found with id={}", id);
                    return new ResourceNotFoundException("Build task not found: " + id);
                });

        if (request.getName() != null) {
            task.setName(request.getName());
        }

        if (request.getRepositoryUrl() != null) {
            task.setRepositoryUrl(request.getRepositoryUrl());
        }

        if (request.getBranch() != null) {
            task.setBranch(request.getBranch());
        }

        if (request.getStatus() != null) {
            BuildStatus newStatus = request.getStatus();

            if (newStatus == BuildStatus.RUNNING && task.getStartedAt() == null) {
                task.setStartedAt(LocalDateTime.now());
            }

            if ((newStatus == BuildStatus.SUCCESS || newStatus == BuildStatus.FAILED)
                    && task.getCompletedAt() == null) {
                task.setCompletedAt(LocalDateTime.now());
            }

            task.setStatus(newStatus);
        }
        if (request.getLogs() != null) {
            task.setLogs(request.getLogs());
        }

        BuildTask updatedTask = repository.save(task);

        log.info("Updated build task with id={}", updatedTask.getId());

        return mapper.toResponse(updatedTask);
    }

    @Override
    public void deleteTask(UUID id) {

        if (!repository.existsById(id)) {
            log.warn("Cannot delete build task because it was not found, id={}", id);
            throw new ResourceNotFoundException("Build task not found: " + id);
        }

        repository.deleteById(id);

        log.info("Deleted build task with id={}", id);
    }
}

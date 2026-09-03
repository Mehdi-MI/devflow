package com.mehdi.devflow.service.impl;

import com.mehdi.devflow.dto.BuildTaskResponse;
import com.mehdi.devflow.dto.CreateBuildTaskRequest;
import com.mehdi.devflow.dto.UpdateBuildTaskRequest;
import com.mehdi.devflow.entity.BuildTask;
import com.mehdi.devflow.enums.BuildStatus;
import com.mehdi.devflow.exception.ResourceNotFoundException;
import com.mehdi.devflow.mapper.BuildTaskMapper;
import com.mehdi.devflow.repository.BuildTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuildTaskServiceImplTest {

    @Mock
    private BuildTaskRepository repository;

    @Mock
    private BuildTaskMapper mapper;

    @InjectMocks
    private BuildTaskServiceImpl service;

    private UUID taskId;
    private BuildTask task;
    private BuildTaskResponse response;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();

        task = BuildTask.builder()
                .id(taskId)
                .name("Test Build")
                .repositoryUrl("https://github.com/test/repo.git")
                .branch("develop")
                .status(BuildStatus.PENDING)
                .build();

        response = BuildTaskResponse.builder()
                .id(taskId)
                .name("Test Build")
                .repositoryUrl("https://github.com/test/repo.git")
                .branch("develop")
                .status(BuildStatus.PENDING)
                .build();
    }

    @Test
    void createTask_shouldSaveTaskAndReturnResponse() {
        CreateBuildTaskRequest request = new CreateBuildTaskRequest();
        request.setName("Test Build");
        request.setRepositoryUrl("https://github.com/test/repo.git");
        request.setBranch("develop");

        when(mapper.toEntity(request)).thenReturn(task);
        when(repository.save(task)).thenReturn(task);
        when(mapper.toResponse(task)).thenReturn(response);

        BuildTaskResponse result = service.createTask(request);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals("Test Build", result.getName());
        assertEquals(BuildStatus.PENDING, result.getStatus());

        verify(mapper).toEntity(request);
        verify(repository).save(task);
        verify(mapper).toResponse(task);
    }

    @Test
    void getAllTasks_shouldReturnAllTasks() {
        BuildTask secondTask = BuildTask.builder()
                .id(UUID.randomUUID())
                .name("Second Build")
                .repositoryUrl("https://github.com/test/second.git")
                .branch("main")
                .status(BuildStatus.SUCCESS)
                .build();

        BuildTaskResponse secondResponse = BuildTaskResponse.builder()
                .id(secondTask.getId())
                .name("Second Build")
                .repositoryUrl("https://github.com/test/second.git")
                .branch("main")
                .status(BuildStatus.SUCCESS)
                .build();

        when(repository.findAll()).thenReturn(List.of(task, secondTask));
        when(mapper.toResponse(task)).thenReturn(response);
        when(mapper.toResponse(secondTask)).thenReturn(secondResponse);

        List<BuildTaskResponse> result = service.getAllTasks();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Build", result.get(0).getName());
        assertEquals("Second Build", result.get(1).getName());

        verify(repository).findAll();
        verify(mapper).toResponse(task);
        verify(mapper).toResponse(secondTask);
    }

    @Test
    void getAllTasks_shouldReturnEmptyListWhenNoTasksExist() {
        when(repository.findAll()).thenReturn(List.of());

        List<BuildTaskResponse> result = service.getAllTasks();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(repository).findAll();
        verifyNoInteractions(mapper);
    }

    @Test
    void getTaskById_shouldReturnTaskWhenFound() {
        when(repository.findById(taskId)).thenReturn(Optional.of(task));
        when(mapper.toResponse(task)).thenReturn(response);

        BuildTaskResponse result = service.getTaskById(taskId);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals("Test Build", result.getName());

        verify(repository).findById(taskId);
        verify(mapper).toResponse(task);
    }

    @Test
    void getTaskById_shouldThrowExceptionWhenTaskDoesNotExist() {
        when(repository.findById(taskId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.getTaskById(taskId)
        );

        assertEquals(
                "Build task not found: " + taskId,
                exception.getMessage()
        );

        verify(repository).findById(taskId);
        verifyNoInteractions(mapper);
    }

    @Test
    void updateTask_shouldUpdateProvidedFields() {
        UpdateBuildTaskRequest request = new UpdateBuildTaskRequest();

        request.setName("Updated Build");
        request.setRepositoryUrl("https://github.com/test/updated.git");
        request.setBranch("main");
        request.setStatus(BuildStatus.RUNNING);
        request.setLogs("Build started");

        when(repository.findById(taskId)).thenReturn(Optional.of(task));
        when(repository.save(task)).thenReturn(task);
        when(mapper.toResponse(task)).thenReturn(response);

        BuildTaskResponse result = service.updateTask(taskId, request);

        assertNotNull(result);

        assertEquals("Updated Build", task.getName());
        assertEquals(
                "https://github.com/test/updated.git",
                task.getRepositoryUrl()
        );
        assertEquals("main", task.getBranch());
        assertEquals(BuildStatus.RUNNING, task.getStatus());
        assertEquals("Build started", task.getLogs());

        verify(repository).findById(taskId);
        verify(repository).save(task);
        verify(mapper).toResponse(task);
    }

    @Test
    void updateTask_shouldOnlyUpdateNonNullFields() {
        UpdateBuildTaskRequest request = new UpdateBuildTaskRequest();

        request.setName("Updated Name");
        request.setStatus(BuildStatus.RUNNING);

        when(repository.findById(taskId)).thenReturn(Optional.of(task));
        when(repository.save(task)).thenReturn(task);
        when(mapper.toResponse(task)).thenReturn(response);

        service.updateTask(taskId, request);

        assertEquals("Updated Name", task.getName());

        assertEquals(
                "https://github.com/test/repo.git",
                task.getRepositoryUrl()
        );

        assertEquals("develop", task.getBranch());

        assertEquals(BuildStatus.RUNNING, task.getStatus());

        assertNull(task.getLogs());

        verify(repository).save(task);
    }

    @Test
    void updateTask_shouldSetStartedAtWhenStatusChangesToRunning() {
        UpdateBuildTaskRequest request = new UpdateBuildTaskRequest();
        request.setStatus(BuildStatus.RUNNING);

        when(repository.findById(taskId)).thenReturn(Optional.of(task));
        when(repository.save(task)).thenReturn(task);
        when(mapper.toResponse(task)).thenReturn(response);

        assertNull(task.getStartedAt());

        service.updateTask(taskId, request);

        assertNotNull(task.getStartedAt());
        assertEquals(BuildStatus.RUNNING, task.getStatus());
        assertNull(task.getCompletedAt());

        verify(repository).save(task);
    }

    @Test
    void updateTask_shouldSetCompletedAtWhenStatusChangesToSuccess() {
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(5);
        task.setStatus(BuildStatus.RUNNING);
        task.setStartedAt(startedAt);

        UpdateBuildTaskRequest request = new UpdateBuildTaskRequest();
        request.setStatus(BuildStatus.SUCCESS);

        when(repository.findById(taskId)).thenReturn(Optional.of(task));
        when(repository.save(task)).thenReturn(task);
        when(mapper.toResponse(task)).thenReturn(response);

        service.updateTask(taskId, request);

        assertEquals(BuildStatus.SUCCESS, task.getStatus());
        assertEquals(startedAt, task.getStartedAt());
        assertNotNull(task.getCompletedAt());
        assertTrue(task.getCompletedAt().isAfter(startedAt));

        verify(repository).save(task);
    }

    @Test
    void updateTask_shouldSetCompletedAtWhenStatusChangesToFailed() {
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(5);
        task.setStatus(BuildStatus.RUNNING);
        task.setStartedAt(startedAt);

        UpdateBuildTaskRequest request = new UpdateBuildTaskRequest();
        request.setStatus(BuildStatus.FAILED);

        when(repository.findById(taskId)).thenReturn(Optional.of(task));
        when(repository.save(task)).thenReturn(task);
        when(mapper.toResponse(task)).thenReturn(response);

        service.updateTask(taskId, request);

        assertEquals(BuildStatus.FAILED, task.getStatus());
        assertEquals(startedAt, task.getStartedAt());
        assertNotNull(task.getCompletedAt());
        assertTrue(task.getCompletedAt().isAfter(startedAt));

        verify(repository).save(task);
    }

    @Test
    void updateTask_shouldNotOverwriteExistingTimestamps() {
        LocalDateTime originalStartedAt = LocalDateTime.now().minusMinutes(10);
        LocalDateTime originalCompletedAt = LocalDateTime.now().minusMinutes(5);

        task.setStatus(BuildStatus.SUCCESS);
        task.setStartedAt(originalStartedAt);
        task.setCompletedAt(originalCompletedAt);

        UpdateBuildTaskRequest request = new UpdateBuildTaskRequest();
        request.setStatus(BuildStatus.SUCCESS);

        when(repository.findById(taskId)).thenReturn(Optional.of(task));
        when(repository.save(task)).thenReturn(task);
        when(mapper.toResponse(task)).thenReturn(response);

        service.updateTask(taskId, request);

        assertEquals(originalStartedAt, task.getStartedAt());
        assertEquals(originalCompletedAt, task.getCompletedAt());
        assertEquals(BuildStatus.SUCCESS, task.getStatus());

        verify(repository).save(task);
    }

    @Test
    void updateTask_shouldThrowExceptionWhenTaskDoesNotExist() {
        UpdateBuildTaskRequest request = new UpdateBuildTaskRequest();
        request.setName("Updated Build");

        when(repository.findById(taskId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.updateTask(taskId, request)
        );

        assertEquals(
                "Build task not found: " + taskId,
                exception.getMessage()
        );

        verify(repository).findById(taskId);
        verify(repository, never()).save(any());
        verifyNoInteractions(mapper);
    }

    @Test
    void deleteTask_shouldDeleteTaskWhenItExists() {
        when(repository.existsById(taskId)).thenReturn(true);

        service.deleteTask(taskId);

        verify(repository).existsById(taskId);
        verify(repository).deleteById(taskId);
    }

    @Test
    void deleteTask_shouldThrowExceptionWhenTaskDoesNotExist() {
        when(repository.existsById(taskId)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.deleteTask(taskId)
        );

        assertEquals(
                "Build task not found: " + taskId,
                exception.getMessage()
        );

        verify(repository).existsById(taskId);
        verify(repository, never()).deleteById(any());
    }
}

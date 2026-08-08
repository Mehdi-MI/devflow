package com.mehdi.devflow.service.impl;

import com.mehdi.devflow.dto.BuildTaskResponse;
import com.mehdi.devflow.dto.CreateBuildTaskRequest;
import com.mehdi.devflow.dto.UpdateBuildTaskRequest;
import com.mehdi.devflow.entity.BuildTask;
import com.mehdi.devflow.enums.BuildStatus;
import com.mehdi.devflow.mapper.BuildTaskMapper;
import com.mehdi.devflow.repository.BuildTaskRepository;
import com.mehdi.devflow.service.BuildTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class BuildTaskServiceIntegrationTest {

    @Autowired
    private BuildTaskService service;

    @Autowired
    private BuildTaskRepository repository;

    @Autowired
    private BuildTaskMapper mapper;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void createTask_shouldPersistTaskInPostgreSQL() {

        CreateBuildTaskRequest request = new CreateBuildTaskRequest();

        request.setName("Integration Test Build");
        request.setRepositoryUrl(
                "https://github.com/test/integration-repo.git"
        );
        request.setBranch("develop");

        BuildTaskResponse response = service.createTask(request);

        assertNotNull(response.getId());
        assertEquals("Integration Test Build", response.getName());
        assertEquals(
                "https://github.com/test/integration-repo.git",
                response.getRepositoryUrl()
        );
        assertEquals("develop", response.getBranch());
        assertEquals(BuildStatus.PENDING, response.getStatus());

        BuildTask savedTask = repository.findById(response.getId())
                .orElseThrow();

        assertEquals(
                "Integration Test Build",
                savedTask.getName()
        );
        assertEquals(
                BuildStatus.PENDING,
                savedTask.getStatus()
        );
    }

    @Test
    void getTaskById_shouldRetrievePersistedTask() {

        BuildTask task = BuildTask.builder()
                .name("Existing Build")
                .repositoryUrl(
                        "https://github.com/test/repo.git"
                )
                .branch("main")
                .status(BuildStatus.PENDING)
                .build();

        BuildTask savedTask = repository.save(task);

        BuildTaskResponse response =
                service.getTaskById(savedTask.getId());

        assertEquals(savedTask.getId(), response.getId());
        assertEquals("Existing Build", response.getName());
        assertEquals("main", response.getBranch());
        assertEquals(BuildStatus.PENDING, response.getStatus());
    }

    @Test
    void getAllTasks_shouldReturnPersistedTasks() {

        BuildTask task1 = BuildTask.builder()
                .name("Build One")
                .repositoryUrl(
                        "https://github.com/test/repo-one.git"
                )
                .branch("main")
                .status(BuildStatus.PENDING)
                .build();

        BuildTask task2 = BuildTask.builder()
                .name("Build Two")
                .repositoryUrl(
                        "https://github.com/test/repo-two.git"
                )
                .branch("develop")
                .status(BuildStatus.RUNNING)
                .build();

        repository.save(task1);
        repository.save(task2);

        List<BuildTaskResponse> tasks =
                service.getAllTasks();

        assertEquals(2, tasks.size());
        assertTrue(
                tasks.stream()
                        .anyMatch(task ->
                                task.getName().equals("Build One"))
        );
        assertTrue(
                tasks.stream()
                        .anyMatch(task ->
                                task.getName().equals("Build Two"))
        );
    }

    @Test
    void updateTask_shouldUpdatePersistedTask() {

        BuildTask task = BuildTask.builder()
                .name("Original Build")
                .repositoryUrl(
                        "https://github.com/test/repo.git"
                )
                .branch("main")
                .status(BuildStatus.PENDING)
                .build();

        BuildTask savedTask = repository.save(task);

        UpdateBuildTaskRequest request =
                new UpdateBuildTaskRequest();

        request.setName("Updated Build");
        request.setBranch("develop");
        request.setStatus(BuildStatus.RUNNING);
        request.setLogs("Build started successfully.");

        BuildTaskResponse response =
                service.updateTask(
                        savedTask.getId(),
                        request
                );

        assertEquals("Updated Build", response.getName());
        assertEquals("develop", response.getBranch());
        assertEquals(BuildStatus.RUNNING, response.getStatus());
        assertEquals(
                "Build started successfully.",
                response.getLogs()
        );

        BuildTask updatedTask =
                repository.findById(savedTask.getId())
                        .orElseThrow();

        assertEquals(
                "Updated Build",
                updatedTask.getName()
        );
        assertEquals(
                "develop",
                updatedTask.getBranch()
        );
        assertEquals(
                BuildStatus.RUNNING,
                updatedTask.getStatus()
        );
        assertEquals(
                "Build started successfully.",
                updatedTask.getLogs()
        );
    }

    @Test
    void deleteTask_shouldRemoveTaskFromPostgreSQL() {

        BuildTask task = BuildTask.builder()
                .name("Build To Delete")
                .repositoryUrl(
                        "https://github.com/test/repo.git"
                )
                .branch("main")
                .status(BuildStatus.PENDING)
                .build();

        BuildTask savedTask = repository.save(task);

        UUID id = savedTask.getId();

        assertTrue(repository.existsById(id));

        service.deleteTask(id);

        assertFalse(repository.existsById(id));
    }

    @Test
    void repository_shouldFindTasksByStatus() {

        BuildTask pendingTask = BuildTask.builder()
                .name("Pending Build")
                .repositoryUrl(
                        "https://github.com/test/pending.git"
                )
                .branch("main")
                .status(BuildStatus.PENDING)
                .build();

        BuildTask runningTask = BuildTask.builder()
                .name("Running Build")
                .repositoryUrl(
                        "https://github.com/test/running.git"
                )
                .branch("develop")
                .status(BuildStatus.RUNNING)
                .build();

        repository.save(pendingTask);
        repository.save(runningTask);

        List<BuildTask> runningTasks =
                repository.findByStatus(BuildStatus.RUNNING);

        assertEquals(1, runningTasks.size());
        assertEquals(
                "Running Build",
                runningTasks.get(0).getName()
        );
    }

    @Test
    void repository_shouldFindTasksByBranch() {

        BuildTask mainTask = BuildTask.builder()
                .name("Main Build")
                .repositoryUrl(
                        "https://github.com/test/main.git"
                )
                .branch("main")
                .status(BuildStatus.PENDING)
                .build();

        BuildTask developTask = BuildTask.builder()
                .name("Develop Build")
                .repositoryUrl(
                        "https://github.com/test/develop.git"
                )
                .branch("develop")
                .status(BuildStatus.PENDING)
                .build();

        repository.save(mainTask);
        repository.save(developTask);

        List<BuildTask> developTasks =
                repository.findByBranch("develop");

        assertEquals(1, developTasks.size());
        assertEquals(
                "Develop Build",
                developTasks.get(0).getName()
        );
    }
}

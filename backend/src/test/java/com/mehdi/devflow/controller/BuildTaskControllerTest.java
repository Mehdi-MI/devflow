package com.mehdi.devflow.controller;

import com.mehdi.devflow.dto.BuildTaskResponse;
import com.mehdi.devflow.dto.CreateBuildTaskRequest;
import com.mehdi.devflow.dto.UpdateBuildTaskRequest;
import com.mehdi.devflow.enums.BuildStatus;
import com.mehdi.devflow.exception.GlobalExceptionHandler;
import com.mehdi.devflow.exception.ResourceNotFoundException;
import com.mehdi.devflow.service.BuildTaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BuildTaskController.class)
@Import(GlobalExceptionHandler.class)
class BuildTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private BuildTaskService service;

    @Test
    void createTask_shouldReturn201Created() throws Exception {
        CreateBuildTaskRequest request = new CreateBuildTaskRequest();
        request.setName("Test Build");
        request.setRepositoryUrl("https://github.com/test/repo.git");
        request.setBranch("develop");

        UUID id = UUID.randomUUID();

        BuildTaskResponse response = BuildTaskResponse.builder()
                .id(id)
                .name("Test Build")
                .repositoryUrl("https://github.com/test/repo.git")
                .branch("develop")
                .status(BuildStatus.PENDING)
                .build();

        when(service.createTask(any(CreateBuildTaskRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/build-tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Test Build"))
                .andExpect(jsonPath("$.repositoryUrl")
                        .value("https://github.com/test/repo.git"))
                .andExpect(jsonPath("$.branch").value("develop"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(service).createTask(any(CreateBuildTaskRequest.class));
    }

    @Test
    void getAllTasks_shouldReturn200Ok() throws Exception {
        UUID id = UUID.randomUUID();

        BuildTaskResponse response = BuildTaskResponse.builder()
                .id(id)
                .name("Test Build")
                .repositoryUrl("https://github.com/test/repo.git")
                .branch("develop")
                .status(BuildStatus.PENDING)
                .build();

        when(service.getAllTasks()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/build-tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].name").value("Test Build"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(service).getAllTasks();
    }

    @Test
    void getAllTasks_shouldReturnEmptyArrayWhenNoTasksExist()
            throws Exception {

        when(service.getAllTasks()).thenReturn(List.of());

        mockMvc.perform(get("/api/build-tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(service).getAllTasks();
    }

    @Test
    void getTaskById_shouldReturn200Ok() throws Exception {
        UUID id = UUID.randomUUID();

        BuildTaskResponse response = BuildTaskResponse.builder()
                .id(id)
                .name("Test Build")
                .repositoryUrl("https://github.com/test/repo.git")
                .branch("develop")
                .status(BuildStatus.PENDING)
                .build();

        when(service.getTaskById(id)).thenReturn(response);

        mockMvc.perform(get("/api/build-tasks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Test Build"))
                .andExpect(jsonPath("$.branch").value("develop"));

        verify(service).getTaskById(id);
    }

    @Test
    void getTaskById_shouldReturn404WhenTaskDoesNotExist()
            throws Exception {

        UUID id = UUID.randomUUID();

        when(service.getTaskById(id))
                .thenThrow(new ResourceNotFoundException(
                        "Build task not found: " + id
                ));

        mockMvc.perform(get("/api/build-tasks/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Build task not found: " + id));

        verify(service).getTaskById(id);
    }

    @Test
    void updateTask_shouldReturn200Ok() throws Exception {
        UUID id = UUID.randomUUID();

        UpdateBuildTaskRequest request = new UpdateBuildTaskRequest();
        request.setName("Updated Build");
        request.setStatus(BuildStatus.RUNNING);

        BuildTaskResponse response = BuildTaskResponse.builder()
                .id(id)
                .name("Updated Build")
                .repositoryUrl("https://github.com/test/repo.git")
                .branch("develop")
                .status(BuildStatus.RUNNING)
                .build();

        when(service.updateTask(
                eq(id),
                any(UpdateBuildTaskRequest.class)
        )).thenReturn(response);

        mockMvc.perform(put("/api/build-tasks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Updated Build"))
                .andExpect(jsonPath("$.status").value("RUNNING"));

        verify(service).updateTask(
                eq(id),
                any(UpdateBuildTaskRequest.class)
        );
    }

    @Test
    void deleteTask_shouldReturn204NoContent() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(service).deleteTask(id);

        mockMvc.perform(delete("/api/build-tasks/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(service).deleteTask(id);
    }

    @Test
    void deleteTask_shouldReturn404WhenTaskDoesNotExist()
            throws Exception {

        UUID id = UUID.randomUUID();

        doThrow(new ResourceNotFoundException(
                "Build task not found: " + id
        )).when(service).deleteTask(id);

        mockMvc.perform(delete("/api/build-tasks/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Build task not found: " + id));

        verify(service).deleteTask(id);
    }

    @Test
    void createTask_shouldReturn400WhenNameIsMissing()
            throws Exception {

        CreateBuildTaskRequest request = new CreateBuildTaskRequest();
        request.setRepositoryUrl("https://github.com/test/repo.git");
        request.setBranch("develop");

        mockMvc.perform(post("/api/build-tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Task name is required"));

        verifyNoInteractions(service);
    }
}

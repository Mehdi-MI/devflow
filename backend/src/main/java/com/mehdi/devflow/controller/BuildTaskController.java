package com.mehdi.devflow.controller;

import com.mehdi.devflow.dto.BuildTaskResponse;
import com.mehdi.devflow.dto.CreateBuildTaskRequest;
import com.mehdi.devflow.dto.UpdateBuildTaskRequest;
import com.mehdi.devflow.service.BuildTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/build-tasks")
@RequiredArgsConstructor
public class BuildTaskController {

    private final BuildTaskService service;

    @PostMapping
    public ResponseEntity<BuildTaskResponse> createTask(
            @Valid @RequestBody CreateBuildTaskRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createTask(request));
    }

    @GetMapping
    public ResponseEntity<List<BuildTaskResponse>> getAllTasks() {

        return ResponseEntity.ok(service.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BuildTaskResponse> getTaskById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(service.getTaskById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BuildTaskResponse> updateTask(
            @PathVariable UUID id,
            @RequestBody UpdateBuildTaskRequest request) {

        return ResponseEntity.ok(service.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID id) {

        service.deleteTask(id);

        return ResponseEntity.noContent().build();
    }
}

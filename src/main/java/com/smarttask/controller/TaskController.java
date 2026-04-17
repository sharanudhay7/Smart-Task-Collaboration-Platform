package com.smarttask.controller;

import com.smarttask.config.PaginationConfig;
import com.smarttask.dto.TaskResponse;
import com.smarttask.entity.Task;
import com.smarttask.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;
    private final PaginationConfig config;

    // ================= GET ALL TASKS =================
    @PreAuthorize("isAuthenticated()") //  Any logged-in user
    @GetMapping
    public Page<TaskResponse> getTasks(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction) {

        return taskService.getAllTasks(
                page != null ? page : config.getDefaultPage(),
                size != null ? size : config.getDefaultSize(),
                sortBy != null ? sortBy : config.getDefaultSort(),
                direction != null ? direction : config.getDefaultDirection()
        );
    }

    // ================= CREATE TASK =================
    @PreAuthorize("hasRole('ADMIN')") //  Only ADMIN
    @PostMapping
    public ResponseEntity<TaskResponse> create(
            @RequestBody Task task) {
        log.info("Request received to create task ");
        log.debug("Task payload: {}", task);
        TaskResponse createdTask = taskService.createTask(task);
        log.info("Task created successfully with id={}", createdTask.getId());
        return ResponseEntity.ok(createdTask);
    }

    // ================= UPDATE TASK =================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(
            @PathVariable Long id,
            @RequestBody Task task) {
        log.info("Request received to update task id={}", id);
        log.debug("Updated task payload: {}", task);
        TaskResponse updatedTask = taskService.updateTask(id, task);
        log.info("Task updated successfully id={}", id);
        return ResponseEntity.ok(updatedTask);
    }

    // ================= ASSIGN TASK =================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{taskId}/assign/{userId}")
    public TaskResponse assignTask(
            @PathVariable Long taskId,
            @PathVariable Long userId) {
        log.info("Request received to assign task id={}", userId);
        return taskService.assignTask(taskId, userId);
    }

    // ================= COMPLETE TASK =================
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{taskId}/complete")
    public TaskResponse completeTask(
            @PathVariable Long taskId) {
        log.info("Request received to completeTask task id={}", taskId);
        return taskService.completeTask(taskId);
    }
}
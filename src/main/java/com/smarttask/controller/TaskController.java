package com.smarttask.controller;

import com.smarttask.dto.TaskResponse;
import com.smarttask.entity.Task;
import com.smarttask.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;

    // ================= GET ALL TASKS =================
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        log.info("Received request to fetch all tasks");
        List<TaskResponse> tasks = taskService.getAllTasks();
        log.info("Fetched {} tasks successfully", tasks.size());
        return ResponseEntity.ok(tasks);
    }

    // ================= CREATE TASK =================
    @PostMapping
    public ResponseEntity<Task> create(
            @RequestBody Task task,
            @RequestParam Long userId) {
        log.info("Request received to create task for userId={}", userId);
        log.debug("Task payload: {}", task);
        Task createdTask = taskService.createTask(task, userId);
        log.info("Task created successfully with id={}", createdTask.getId());
        return ResponseEntity.ok(createdTask);
    }

    // ================= UPDATE TASK =================
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
    @PutMapping("/{taskId}/assign/{userId}")
    public TaskResponse assignTask(
            @PathVariable Long taskId,
            @PathVariable Long userId) {
        log.info("Request received to assign task id={}", userId);
        return taskService.assignTask(taskId, userId);
    }

    // ================= COMPLETE TASK =================
    @PutMapping("/{taskId}/complete")
    public TaskResponse completeTask(
            @PathVariable Long taskId) {
        log.info("Request received to completeTask task id={}", taskId);
        return taskService.completeTask(taskId);
    }
}
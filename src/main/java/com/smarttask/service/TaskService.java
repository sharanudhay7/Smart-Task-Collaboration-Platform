package com.smarttask.service;

import com.smarttask.dto.TaskResponse;
import com.smarttask.entity.AuditLog;
import com.smarttask.entity.Task;
import com.smarttask.entity.User;
import com.smarttask.enums.Status;
import com.smarttask.exception.UserNotFoundException;
import com.smarttask.repository.AuditRepository;
import com.smarttask.repository.TaskRepository;
import com.smarttask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AuditRepository auditRepository;

    // ================= CREATE TASK =================
    @CacheEvict(value = "tasks", allEntries = true)
    public Task createTask(Task task, Long userId) {

        log.info("Creating task for userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with id={}", userId);
                    return new UserNotFoundException(
                            "User not found with id: " + userId);
                });

        task.setAssignedTo(user);
        task.setStatus(Status.OPEN);

        Task saved = taskRepository.save(task);

        log.info("Task created successfully. TaskId={}, AssignedTo={}",
                saved.getId(), user.getUsername());

        saveAudit("Task Created", user.getUsername());

        return saved;
    }

    // ================= UPDATE TASK =================
    // WRITE OPERATION → INVALIDATE CACHE
    @CacheEvict(value = "tasks", allEntries = true)
    public TaskResponse updateTask(Long id, Task request) {

        log.info("Updating task with id={}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Task not found with id={}", id);
                    return new RuntimeException("Task not found");
                });

        if (request.getTitle() != null) {
            log.debug("Updating title for taskId={}", id);
            task.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            log.debug("Updating description for taskId={}", id);
            task.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            log.debug("Updating status for taskId={} to {}", id, request.getStatus());
            task.setStatus(request.getStatus());
        }

        if (request.getPriority() != null) {
            log.debug("Updating priority for taskId={} to {}", id, request.getPriority());
            task.setPriority(request.getPriority());
        }

        taskRepository.save(task);

        saveAudit("Task Updated", "SYSTEM");

        log.info("Task updated successfully. taskId={}", id);

        return mapToResponse(task);
    }

    // ================= GET ALL TASKS =================
    @Cacheable(value = "tasks")
    public List<TaskResponse> getAllTasks() {

        log.info("Fetching all tasks");

        List<Task> tasks = taskRepository.findAll();

        log.info("Total tasks fetched={}", tasks.size());

        return tasks.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ================= AUDIT =================
    private void saveAudit(String action, String user) {

        log.debug("Saving audit log. Action={}, User={}", action, user);

        AuditLog logEntity = new AuditLog();
        logEntity.setAction(action);
        logEntity.setChangedBy(user);
        logEntity.setChangedAt(LocalDateTime.now());

        auditRepository.save(logEntity);
    }

//Task Assign
    @CacheEvict(value = "tasks", allEntries = true)
    public TaskResponse assignTask(Long taskId, Long userId) {

        log.info("Assigning task {} to user {}", taskId, userId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        task.setAssignedTo(user);
        //task.setStatus(Status.IN_PROGRESS);

        taskRepository.save(task);

        saveAudit("Task Assigned", user.getUsername());

        return mapToResponse(task);
    }

    //=============CompleteTask=====
    @CacheEvict(value = "tasks", allEntries = true)
    public TaskResponse completeTask(Long taskId) {

        log.info("Completing task {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setStatus(Status.DONE);

        taskRepository.save(task);

        saveAudit("Task Completed", "SYSTEM");

        return mapToResponse(task);
    }

    // ================= MAPPER =================
    private TaskResponse mapToResponse(Task task) {

        log.debug("Mapping task to response. taskId={}", task.getId());

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .build();
    }
}
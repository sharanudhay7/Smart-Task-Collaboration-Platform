package com.smarttask.service;

import com.smarttask.dto.TaskResponse;
import com.smarttask.dto.UserDto;
import com.smarttask.entity.AuditLog;
import com.smarttask.entity.Task;
import com.smarttask.entity.User;
import com.smarttask.enums.Role;
import com.smarttask.enums.Status;
import com.smarttask.exception.UserNotFoundException;
import com.smarttask.repository.AuditRepository;
import com.smarttask.repository.TaskRepository;
import com.smarttask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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

    public TaskResponse createTask(Task task) {

        log.info("Creating task");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email={}", email);
                    return new UserNotFoundException(
                            "User not found with email: " + email);
                });

        if (!(user.getRole() == Role.ADMIN ||
                user.getRole() == Role.MANAGER)) {
            throw new RuntimeException("Not authorized to create task");
        }

        task.setAssignedTo(user);
        task.setStatus(Status.OPEN);

        Task saved = taskRepository.save(task);

        log.info("Task created successfully. TaskId={}, AssignedTo={}",
                saved.getId(), user.getUsername());

       saveAudit(
               saved,          // Task
               user,           // User
               "CREATE",       // Action
               null,           // fieldName
               null,           // oldValue
               null            // newValue
       );

        return mapToResponse(saved);
    }
    // ================= UPDATE TASK =================
    // WRITE OPERATION → INVALIDATE CACHE
    @CacheEvict(value = "tasks", allEntries = true)
    public TaskResponse updateTask(Long id, Task request) {

        log.info("Updating task with id={}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only ADMIN / MANAGER
        if (!(currentUser.getRole() == Role.ADMIN ||
                currentUser.getRole() == Role.MANAGER)) {
            throw new RuntimeException("Not authorized to update task");
        }

        //Capture OLD values
        String oldTitle = task.getTitle();
        String oldDescription = task.getDescription();
        String oldStatus = task.getStatus() != null ? task.getStatus().name() : null;
        String oldPriority = task.getPriority() != null ? task.getPriority().name() : null;

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

        Task updatedTask = taskRepository.save(task);

        log.info("Task updated successfully. taskId={}", id);

        // Compare & Audit

        auditIfChanged(updatedTask, currentUser, "title",
                oldTitle, updatedTask.getTitle());

        auditIfChanged(updatedTask, currentUser, "description",
                oldDescription, updatedTask.getDescription());

        auditIfChanged(updatedTask, currentUser, "status",
                oldStatus,
                updatedTask.getStatus() != null ? updatedTask.getStatus().name() : null);

        auditIfChanged(updatedTask, currentUser, "priority",
                oldPriority,
                updatedTask.getPriority() != null ? updatedTask.getPriority().name() : null);

        log.info("Task updated successfully. taskId={}", id);

        return mapToResponse(updatedTask);
    }


    // ================= GET ALL TASKS =================
    @Cacheable(value = "tasks")
    @Transactional(readOnly = true)
    public Page<TaskResponse>  getAllTasks(int page, int size, String sortBy, String direction) {

        log.info("Fetching all tasks with pagination");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Sorting logic
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Task> taskPage;

        if (currentUser.getRole() == Role.USER) {
            //  Only assigned tasks (paginated)
            taskPage = taskRepository.findByAssignedToId(currentUser.getId(), pageable);
        } else {
            //  ADMIN / MANAGER (all tasks paginated)
            taskPage = taskRepository.findAll(pageable);
        }

        // Convert Entity → DTO
        return taskPage.map(this::mapToResponse);
    }

    // ================= AUDIT =================
    private void saveAudit(Task task,
                           User user,
                           String action,
                           String fieldName,
                           String oldValue,
                           String newValue) {

        log.debug("Saving audit log. Action={}, TaskId={}, UserId={}",
                action,
                task != null ? task.getId() : null,
                user != null ? user.getId() : null);

        AuditLog audit = new AuditLog();
        audit.setTask(task);
        audit.setUser(user);
        audit.setAction(action);
        audit.setFieldName(fieldName);
        audit.setOldValue(oldValue);
        audit.setNewValue(newValue);
        audit.setChangedAt(LocalDateTime.now());

        auditRepository.save(audit);
    }

//Task Assign
    @CacheEvict(value = "tasks", allEntries = true)
    public TaskResponse assignTask(Long taskId, Long userId) {

        log.info("Assigning task {} to user {}", taskId, userId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Only ADMIN or MANAGER
        if (!(currentUser.getRole().name().equals("ADMIN") ||
                currentUser.getRole().name().equals("MANAGER"))) {
            throw new RuntimeException("Not authorized to assign task");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));
  // Capture old assigned user
        String oldAssignedUser = task.getAssignedTo() != null
                ? task.getAssignedTo().getUsername()
                : null;

        task.setAssignedTo(user);

        Task updatedTask = taskRepository.save(task);

        // STEP 3: Audit (OLD → NEW)
        saveAudit(
                updatedTask,
                currentUser,              // who performed action
                "ASSIGN",                 // action type
                "assignedTo",             // field changed
                oldAssignedUser,          // old value
                user.getUsername()     // new value
        );


        return mapToResponse(task);
    }

    //=============CompleteTask=====
    @CacheEvict(value = "tasks", allEntries = true)
    public TaskResponse completeTask(Long taskId) {

        log.info("Completing task {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Only assigned user or ADMIN
        if (!(task.getAssignedTo().getId().equals(currentUser.getId()) ||
                currentUser.getRole() == Role.ADMIN)) {
            throw new RuntimeException("Not authorized to complete this task");
        }
        //Capture OLD value
        String oldStatus = task.getStatus().name();
        task.setStatus(Status.DONE);

        Task updatedTask = taskRepository.save(task);

        saveAudit(
                updatedTask,
                currentUser,
                "UPDATE",
                "status",
                oldStatus,
                Status.DONE.name()
        );

        return mapToResponse(task);
    }

    // ================= MAPPER =================
    private TaskResponse mapToResponse(Task task) {

        log.debug("Mapping task to response. taskId={}", task.getId());

        UserDto assignedUser = null;

        if (task.getAssignedTo() != null) {
            assignedUser = new UserDto(
                    task.getAssignedTo().getId(),
                    task.getAssignedTo().getEmail(),
                    task.getAssignedTo().getRole().name(),
                    task.getAssignedTo().getUsername()
            );
        }

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .assignedUser(assignedUser) //
                .build();
    }

    private void auditIfChanged(Task task,
                                User user,
                                String field,
                                String oldVal,
                                String newVal) {

        if (!Objects.equals(oldVal, newVal)) {
            saveAudit(task, user, "UPDATE", field, oldVal, newVal);
        }
    }
}
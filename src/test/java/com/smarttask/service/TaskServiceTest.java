package com.smarttask.service;

import com.smarttask.dto.TaskResponse;
import com.smarttask.entity.AuditLog;
import com.smarttask.entity.Task;
import com.smarttask.entity.User;
import com.smarttask.enums.Priority;
import com.smarttask.enums.Status;
import com.smarttask.exception.UserNotFoundException;
import com.smarttask.repository.AuditRepository;
import com.smarttask.repository.TaskRepository;
import com.smarttask.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditRepository auditRepository;

    @InjectMocks
    private TaskService taskService;

    // ================= CREATE TASK =================

    @Test
    void shouldCreateTaskSuccessfully() {

        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setUsername("john");

        Task task = new Task();
        task.setTitle("New Task");

        Task savedTask = new Task();
        savedTask.setId(100L);
        savedTask.setAssignedTo(user);
        savedTask.setStatus(Status.OPEN);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(taskRepository.save(any(Task.class)))
                .thenReturn(savedTask);

        Task result = taskService.createTask(task, userId);

        assertNotNull(result);
        assertEquals(Status.OPEN, result.getStatus());

        verify(taskRepository).save(any(Task.class));
        verify(auditRepository).save(any(AuditLog.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        Task task = new Task();

        assertThrows(UserNotFoundException.class,
                () -> taskService.createTask(task, 1L));

        verify(taskRepository, never()).save(any());
    }

    // ================= UPDATE TASK =================

    @Test
    void shouldUpdateTaskSuccessfully() {

        Long taskId = 10L;

        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setTitle("Old Title");
        existingTask.setDescription("Old Desc");
        existingTask.setStatus(Status.OPEN);
        existingTask.setPriority(Priority.MEDIUM);

        Task request = new Task();
        request.setTitle("New Title");
        request.setStatus(Status.IN_PROGRESS);

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(existingTask));

        when(taskRepository.save(any(Task.class)))
                .thenReturn(existingTask);

        TaskResponse response =
                taskService.updateTask(taskId, request);

        assertEquals("New Title", response.getTitle());
        assertEquals("IN_PROGRESS", response.getStatus());

        verify(taskRepository).save(existingTask);
        verify(auditRepository).save(any(AuditLog.class));
    }

    @Test
    void shouldThrowExceptionWhenTaskNotFound() {

        when(taskRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> taskService.updateTask(1L, new Task()));

        verify(taskRepository, never()).save(any());
    }

    // ================= GET ALL TASKS =================

    @Test
    void shouldReturnAllTasks() {

        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");
        task1.setDescription("Desc");
        task1.setStatus(Status.OPEN);
        task1.setPriority(Priority.HIGH);

        when(taskRepository.findAll())
                .thenReturn(List.of(task1));

        List<TaskResponse> result =
                taskService.getAllTasks();

        assertEquals(1, result.size());
        assertEquals("Task 1", result.get(0).getTitle());

        verify(taskRepository).findAll();
    }

    // ================= EDGE CASE =================

    @Test
    void shouldReturnEmptyListWhenNoTasksExist() {

        when(taskRepository.findAll())
                .thenReturn(List.of());

        List<TaskResponse> result =
                taskService.getAllTasks();

        assertTrue(result.isEmpty());
    }
}
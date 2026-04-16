/*package com.smarttask.service;

import com.smarttask.entity.Comment;
import com.smarttask.entity.Task;
import com.smarttask.entity.User;
import com.smarttask.repository.CommentRepository;
import com.smarttask.repository.TaskRepository;
import com.smarttask.repository.UserRepository;
import com.smarttask.service.CommentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    // ================= SUCCESS CASE =================

    @Test
    void shouldAddCommentSuccessfully() {

        Long taskId = 1L;
        Long userId = 2L;
        String message = "Nice work";

        Task task = new Task();
        task.setId(taskId);

        User user = new User();
        user.setId(userId);

        Comment savedComment = new Comment();
        savedComment.setMessage(message);
        savedComment.setTask(task);
        savedComment.setUser(user);

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(commentRepository.save(any(Comment.class)))
                .thenReturn(savedComment);

        Comment result =
                commentService.addComment(taskId, userId, message);

        assertNotNull(result);
        assertEquals(message, result.getMessage());

        verify(taskRepository).findById(taskId);
        verify(userRepository).findById(userId);
        verify(commentRepository).save(any(Comment.class));
    }

    // ================= TASK NOT FOUND =================

    @Test
    void shouldThrowExceptionWhenTaskNotFound() {

        when(taskRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> commentService.addComment(1L, 1L, "test"));

        verify(commentRepository, never()).save(any());
    }

    // ================= USER NOT FOUND =================

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        Task task = new Task();
        task.setId(1L);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(userRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> commentService.addComment(1L, 2L, "test"));

        verify(commentRepository, never()).save(any());
    }

    // ================= DATA MAPPING TEST =================

    @Test
    void shouldMapFieldsCorrectly() {

        Task task = new Task();
        task.setId(1L);

        User user = new User();
        user.setId(2L);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Comment result =
                commentService.addComment(1L, 2L, "Hello");

        assertEquals("Hello", result.getMessage());
        assertEquals(task, result.getTask());
        assertEquals(user, result.getUser());
    }
}*/
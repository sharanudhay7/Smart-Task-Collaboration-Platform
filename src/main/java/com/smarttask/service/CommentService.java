package com.smarttask.service;

import com.smarttask.dto.CommentRequest;
import com.smarttask.entity.Comment;
import com.smarttask.entity.Task;
import com.smarttask.entity.User;
import com.smarttask.enums.Role;
import com.smarttask.repository.CommentRepository;
import com.smarttask.repository.TaskRepository;
import com.smarttask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public CommentRequest addComment(Long taskId, CommentRequest request) {

        log.info("Comment addComment request");
        Task taskDetails = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        //  Get logged-in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Authorization check
        if (!canComment(taskDetails, currentUser)) {
            throw new RuntimeException("Not authorized to comment on this task");
        }

        Task task = taskRepository.findById(taskId).orElseThrow();
        User user = userRepository.findById(currentUser.getId()).orElseThrow();

        Comment comment = new Comment();
        comment.setMessage(request.getText());
        comment.setTask(task);
        comment.setUser(user);
        log.info("Comment added successfully");
        commentRepository.save(comment);
        CommentRequest dto = new CommentRequest();
        dto.setText(comment.getMessage());
        return dto;

    }

    private boolean canComment(Task task, User user) {
        return task.getAssignedTo() != null &&
                (task.getAssignedTo().getId().equals(user.getId()) ||
                        user.getRole() == Role.ADMIN ||
                        user.getRole() == Role.MANAGER);
    }
}


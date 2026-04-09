package com.smarttask.service;

import com.smarttask.entity.Comment;
import com.smarttask.entity.Task;
import com.smarttask.entity.User;
import com.smarttask.repository.CommentRepository;
import com.smarttask.repository.TaskRepository;
import com.smarttask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public Comment addComment(Long taskId,
                              Long userId,
                              String message) {

        Task task = taskRepository.findById(taskId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        Comment comment = new Comment();
        comment.setMessage(message);
        comment.setTask(task);
        comment.setUser(user);
        log.info("Comment added successfully");
        return commentRepository.save(comment);
    }
}
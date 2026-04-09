package com.smarttask.controller;

import com.smarttask.entity.Comment;
import com.smarttask.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{id}/comments")
    public Comment comment(@PathVariable Long id,
                           @RequestParam Long userId,
                           @RequestParam String message) {
        log.debug("Received comment for user: {}", message);
        return commentService.addComment(id, userId, message);
    }
}
package com.smarttask.controller;

import com.smarttask.dto.CommentRequest;
import com.smarttask.entity.Comment;
import com.smarttask.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/comments")
    public CommentRequest  comment(@PathVariable Long id,
                           @RequestBody CommentRequest request) {
        log.debug("Received comment for user: {}", request);
        return commentService.addComment(id, request);
    }
}
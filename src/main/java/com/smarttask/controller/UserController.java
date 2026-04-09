package com.smarttask.controller;

import com.smarttask.dto.UserRequest;
import com.smarttask.entity.User;
import com.smarttask.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public User register(@Valid @RequestBody UserRequest user) {
        log.debug("Received request to register user: {}", user.getUsername());
        User savedUser = userService.register(user);
        log.debug("User registered successfully with id: {}", savedUser.getId());
        return savedUser;
    }
}
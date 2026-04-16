package com.smarttask.controller;

import com.smarttask.dto.UserDto;
import com.smarttask.dto.UserRequest;
import com.smarttask.entity.User;
import com.smarttask.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;


    @PreAuthorize("hasRole('ADMIN')")
   @PostMapping
    public UserDto register(@Valid @RequestBody UserRequest user) {
        log.debug("Received request to register user: {}", user.getUsername());
        UserDto savedUser = userService.register(user);
        log.debug("User registered successfully with id: {}", savedUser.getId());
        return savedUser;
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
        @GetMapping
        public List<UserDto> getAllUsers() {
            return userService.getAllUsers();

    }
}
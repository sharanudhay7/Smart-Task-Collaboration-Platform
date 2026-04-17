package com.smarttask.service;

import com.smarttask.dto.UserDto;
import com.smarttask.dto.UserRequest;
import com.smarttask.entity.User;
import com.smarttask.enums.Role;
import com.smarttask.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
//import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDto register(UserRequest request) {
        log.info("User registration started for email: {}", request.getEmail());
        // Encode password
        //user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Save user (DB exceptions will be handled globally)

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.valueOf(request.getRole()));
        userRepository.save(user);
        log.info("User registered successfully. UserId: {}, Email: {}",
                user.getId(),
                user.getEmail());

        return mapToResponse(user);
    }

    @Cacheable(value = "tasks")
    public List<UserDto> getAllUsers() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //Extra safety check
        if (!(currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.MANAGER)) {
            throw new RuntimeException("Not authorized to view users");
        }
        return userRepository.findAll()
                .stream()
                .map(user -> new UserDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getUsername()
                ))
                .toList();
    }


    private UserDto mapToResponse(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUserName(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        return dto;
    }}

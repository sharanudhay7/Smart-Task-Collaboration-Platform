package com.smarttask.service;

import com.smarttask.dto.UserDto;
import com.smarttask.dto.UserRequest;
import com.smarttask.entity.User;
import com.smarttask.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
//import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public User register(UserRequest request) {
        log.info("User registration started for email: {}", request.getEmail());
        // Encode password
        //user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Save user (DB exceptions will be handled globally)

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        log.info("User registered successfully. UserId: {}, Email: {}",
                user.getId(),
                user.getEmail());
        return userRepository.save(user);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail()
                ))
                .toList();
    }
}

package com.smarttask.controller;

import com.smarttask.entity.User;
import com.smarttask.enums.Role;
import com.smarttask.repository.UserRepository;
import com.smarttask.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    @PostMapping("/login")
    public String login(@RequestBody User request) {
        log.info("Login attempt for user={}", request.getUsername());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        String token = jwtService.generateToken(request.getUsername());
        log.info("Login successful for user={}", request.getUsername());
        return token;
    }

//Testing purpose
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        log.info("Register request received for user={}", user.getUsername());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        log.info("User registered successfully: {}", user.getUsername());
        return "User Registered Successfully";
    }
}



    /*@PostMapping("/login")
    public User login(@RequestBody User request) {

        return userRepository
                .findByUsername(request.getUsername())
                .orElseThrow();
    }*/

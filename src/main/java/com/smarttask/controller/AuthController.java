package com.smarttask.controller;

import com.smarttask.dto.AuthResponse;
import com.smarttask.dto.UserDto;
import com.smarttask.entity.User;
import com.smarttask.enums.Role;
import com.smarttask.repository.UserRepository;
import com.smarttask.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
    public ResponseEntity<?> login(@RequestBody User request) {

        log.info("Login attempt for user={}", request.getEmail());

        try {
            // ✅ Authenticate
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // ✅ Fetch user from DB
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ Generate token
            String token = jwtService.generateToken(user.getEmail());

            log.info("Login successful for user={}", user.getEmail());

            // ✅ Prepare response DTO
            UserDto userDto = new UserDto(
                    user.getId(),
                    user.getEmail(),
                    user.getRole().name(),
                    user.getUsername()
            );

            return ResponseEntity.ok(new AuthResponse(token, userDto));

        } catch (BadCredentialsException ex) {
            log.error("Invalid credentials for user={}", request.getEmail());
            return ResponseEntity
                    .status(401)
                    .body("Invalid email or password");

        } catch (Exception ex) {
            log.error("Login error for user={}", request.getEmail(), ex);
            return ResponseEntity
                    .status(500)
                    .body("Something went wrong. Please try again.");
        }
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

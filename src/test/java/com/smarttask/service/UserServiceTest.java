package com.smarttask.service;

import com.smarttask.dto.UserRequest;
import com.smarttask.entity.User;
import com.smarttask.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    // ================= SUCCESS CASE =================
    @Test
    void shouldRegisterUserSuccessfully() {

        // Arrange
        UserRequest request = new UserRequest();
        request.setUsername("saranya");
        request.setEmail("saranya@test.com");
        request.setPassword("password123");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername(request.getUsername());
        savedUser.setEmail(request.getEmail());
        savedUser.setPassword(request.getPassword());

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        // Act
        User result = userService.register(request);

        // Assert
        assertNotNull(result);
        assertEquals("saranya", result.getUsername());
        assertEquals("saranya@test.com", result.getEmail());

        verify(userRepository, times(1))
                .save(any(User.class));
    }

    // ================= EDGE CASE =================
    @Test
    void shouldHandleNullValuesGracefully() {

        UserRequest request = new UserRequest();
        request.setUsername(null);
        request.setEmail(null);
        request.setPassword(null);

        User savedUser = new User();

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        User result = userService.register(request);

        assertNotNull(result);

        verify(userRepository).save(any(User.class));
    }

    // ================= VERIFY DATA MAPPING =================
    @Test
    void shouldMapUserRequestToUserEntity() {

        UserRequest request = new UserRequest();
        request.setUsername("john");
        request.setEmail("john@test.com");
        request.setPassword("123");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.register(request);

        assertEquals("john", result.getUsername());
        assertEquals("john@test.com", result.getEmail());
        assertEquals("123", result.getPassword());
    }
}
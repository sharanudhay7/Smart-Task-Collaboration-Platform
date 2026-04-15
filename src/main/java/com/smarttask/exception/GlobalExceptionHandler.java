package com.smarttask.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Duplicate email / DB constraint error
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<GlobalErrorResponse> handleDatabaseException(
            DataIntegrityViolationException ex) {
        log.error("Database constraint violation", ex);
        GlobalErrorResponse error = GlobalErrorResponse.builder()
                .errorCode("DUPLICATE_RESOURCE")
                .message("Email already exists")
                .status(HttpStatus.CONFLICT.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body((GlobalErrorResponse) error);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException", ex);

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        GlobalErrorResponse error = GlobalErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .message(message)
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    // ResponseStatusException handler
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<GlobalErrorResponse> handleResponseStatus(
            ResponseStatusException ex) {

        log.error("Application exception occurred", ex);

        GlobalErrorResponse error = GlobalErrorResponse.builder()
                .errorCode("APPLICATION_ERROR")
                .message(ex.getReason())
                .status(ex.getStatusCode().value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(error);
    }




    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<GlobalErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex) {
        log.error("Duplicate resource exception: {}", ex.getMessage());
        GlobalErrorResponse error = GlobalErrorResponse.builder()
                .errorCode("DUPLICATE_RESOURCE")
                .message(ex.getMessage())
                .status(HttpStatus.CONFLICT.value())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    //UserId- check
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<GlobalErrorResponse> handleNotFound(
            UserNotFoundException ex) {
        log.error("UserNotFoundException: {}", ex.getMessage());
        GlobalErrorResponse error = GlobalErrorResponse.builder()
                .errorCode("RESOURCE_NOT_FOUND")
                .message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials() {
        return ResponseEntity.status(401).body("Invalid email or password");
    }

    // Fallback exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected system error", ex);
        GlobalErrorResponse error = GlobalErrorResponse.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .message("Something went wrong")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }}
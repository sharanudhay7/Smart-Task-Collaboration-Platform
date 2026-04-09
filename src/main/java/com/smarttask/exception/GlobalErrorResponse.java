package com.smarttask.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GlobalErrorResponse {

    private String errorCode;
    private String message;
    private int status;
    private LocalDateTime timestamp;
}
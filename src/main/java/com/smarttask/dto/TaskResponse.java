package com.smarttask.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private UserDto assignedUser;
}

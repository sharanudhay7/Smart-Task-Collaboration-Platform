package com.smarttask.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class AuditLog {

        @Id
        @GeneratedValue
        private Long id;

        //  Link to Task
        @ManyToOne
        @JoinColumn(name = "task_id")
        private Task task;

        //  Link to User
        @ManyToOne
        @JoinColumn(name = "user_id")
        private User user;

        // Action type
        private String action; // CREATE, UPDATE, DELETE

        // What field changed (optional but useful)
        private String fieldName;

        // Old & New values
        private String oldValue;
        private String newValue;

        private LocalDateTime changedAt;
    }

package com.smarttask.service;

import com.smarttask.entity.Task;
import com.smarttask.entity.User;
import com.smarttask.enums.Role;
import com.smarttask.repository.TaskRepository;
import com.smarttask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Cacheable(value = "dashboard")
    public Map<String,Object> dashboard() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User currentUser = userRepository.findByEmail(email).orElseThrow();

        Map<String,Object> response = new HashMap<>();

        if (currentUser.getRole() == Role.USER) {

            //  Only their tasks
            List<Task> tasks = taskRepository.findByAssignedToId(currentUser.getId());

            response.put("tasksByStatus",
                    tasks.stream().collect(Collectors.groupingBy(
                            t -> t.getStatus().name(),
                            Collectors.counting()
                    )));

        } else {
            // ADMIN / MANAGER → full data
            response.put("tasksByStatus", taskRepository.countTasksByStatus());
            response.put("overdueTasks", taskRepository.countOverdueTasks());
        }

        return response;
    }
    }


package com.smarttask.service;

import com.smarttask.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TaskRepository taskRepository;

    @Cacheable(value = "dashboard")
    public Map<String,Object> dashboard() {

        Map<String,Object> response = new HashMap<>();

        response.put("tasksByStatus",
                taskRepository.countTasksByStatus());

        response.put("overdueTasks",
                taskRepository.countOverdueTasks());

        response.put("tasksPerUser",
                taskRepository.findAll()
                        .stream()
                        .collect(Collectors.groupingBy(
                                t -> t.getAssignedTo().getUsername(),
                                Collectors.counting()
                        )));

        return response;
    }
}

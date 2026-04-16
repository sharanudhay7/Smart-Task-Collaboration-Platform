package com.smarttask.repository;

import com.smarttask.entity.Task;
import com.smarttask.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(Status status);

    @Query("SELECT t.status, COUNT(t)  FROM Task t GROUP BY t.status")
    List<Object[]> countTasksByStatus();

    @Query("SELECT COUNT(t) FROM Task t WHERE t.dueDate < CURRENT_DATE")
    long countOverdueTasks();

    List<Task> findByAssignedToId(Long id);
}

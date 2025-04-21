package com.example.demo.javaSrc.eventsANDtask;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserId(Long userId);

    List<Task> findByUserIdAndDeadline(Long userId, Date date);

    boolean TaskIsCompleted(Long userId, Long taskId);

    void markTaskAsCompleted(Long userId, Long taskId);

}

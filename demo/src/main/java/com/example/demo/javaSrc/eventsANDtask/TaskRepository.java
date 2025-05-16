package com.example.demo.javaSrc.eventsANDtask;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findBySchoolId(Long schoolId);
    List<Task> findBySchoolIdAndClassId(Long schoolId, Long classId);
    Optional<Task> findById(Long id);
}


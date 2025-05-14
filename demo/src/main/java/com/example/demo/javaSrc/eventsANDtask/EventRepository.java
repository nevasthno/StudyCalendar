package com.example.demo.javaSrc.eventsANDtask;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
          // Search by creator and date
    List<Event> findByCreatedByAndStartEventAfter(Long createdBy, LocalDateTime date);
    List<Event> findByCreatedByAndStartEventBefore(Long createdBy, LocalDateTime date);

    // Search by title
    List<Event> findByCreatedByAndTitleContainingIgnoreCase(Long createdBy, String title);

    // Search by date
    List<Event> findByCreatedByAndStartEventBetween(Long createdBy, LocalDateTime start, LocalDateTime end);
}

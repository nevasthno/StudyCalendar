package com.example.demo.javaSrc.eventsANDtask;

import java.io.File;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByUserId(Long userId);

    File getFileByEventId(Long eventId);

    String commentEvent(Event event, String comment);
}

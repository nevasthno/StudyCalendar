package com.example.demo.javaSrc.eventsANDtask;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository eventRepository;

    @Autowired
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

      public List<Event> getFutureEvents(Long userId) {
        return eventRepository.findByCreatedByAndStartEventAfter(userId, LocalDateTime.now());
    }

    public List<Event> getPastEvents(Long userId) {
        return eventRepository.findByCreatedByAndStartEventBefore(userId, LocalDateTime.now());
    }

    public List<Event> searchByTitle(Long userId, String keyword) {
        return eventRepository.findByCreatedByAndTitleContainingIgnoreCase(userId, keyword);
    }

    public List<Event> searchByDateRange(Long userId, LocalDateTime from, LocalDateTime to) {
        return eventRepository.findByCreatedByAndStartEventBetween(userId, from, to);
    }
}

package com.example.demo.javaSrc.worker;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.javaSrc.eventsANDtask.Event;
import com.example.demo.javaSrc.eventsANDtask.EventService;
import com.example.demo.javaSrc.eventsANDtask.Task;
import com.example.demo.javaSrc.eventsANDtask.TaskService;
import com.example.demo.javaSrc.people.People;
import com.example.demo.javaSrc.people.PeopleService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final TaskService taskService;
    private final EventService eventService;
    private final PeopleService peopleService;
    private final PasswordEncoder passwordEncoder;        // ← 

    @Autowired
    public ApiController(TaskService taskService,
                         EventService eventService,
                         PeopleService peopleService,
                         PasswordEncoder passwordEncoder) {  // ← 
        this.taskService    = taskService;
        this.eventService   = eventService;
        this.peopleService  = peopleService;
        this.passwordEncoder = passwordEncoder;           // ← 
    }

    @GetMapping("/tasks")
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/events")
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/teachers")
    public List<People> getTeachers() {
        return peopleService.getPeopleByRole("TEACHER");
    }

    @PostMapping("/tasks/{id}/toggle-complete")
    public ResponseEntity<Void> toggleTask(@PathVariable Long id) {
        taskService.toggleComplete(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/users")
    public ResponseEntity<People> createUser(@RequestBody People newUser) {
        String raw = newUser.getPassword();
        newUser.setPassword(passwordEncoder.encode(raw));
        People saved = peopleService.createPeople(newUser);
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/tasks")
    public ResponseEntity<Task> createTask(@RequestBody Task newTask) {
        Task saved = taskService.createTask(newTask);
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/events")
    public ResponseEntity<Event> createEvent(@RequestBody Event newEvent,
                                             Authentication auth) {
        String email = auth.getName(); 
        People teacher = peopleService.findByEmail(email);
        newEvent.setCreatedBy(teacher.getId());

        Event saved = eventService.createEvent(newEvent);
        return ResponseEntity.ok(saved);
    }
    
    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        List<Task> all = taskService.getAllTasks();
        long totalTasks = all.size();
        long completedTasks = all.stream().filter(Task::isCompleted).count();
        long totalEvents = eventService.getAllEvents().size();
        return Map.of(
            "totalTasks", totalTasks,
            "completedTasks", completedTasks,
            "totalEvents", totalEvents
        );
    }

    @GetMapping("/future/{userId}")
    public List<Event> getFutureEvents(@PathVariable Long userId) {
        return eventService.getFutureEvents(userId);
    }

    @GetMapping("/past/{userId}")
    public List<Event> getPastEvents(@PathVariable Long userId) {
        return eventService.getPastEvents(userId);
    }

    @GetMapping("/search/title")
    public List<Event> searchByTitle(
        @RequestParam Long userId,
        @RequestParam String keyword) {
        return eventService.searchByTitle(userId, keyword);
    }

    @GetMapping("/search/date")
    public List<Event> searchByDateRange(
        @RequestParam Long userId,
        @RequestParam String from,
        @RequestParam String to) {
        return eventService.searchByDateRange(
            userId,
            LocalDateTime.parse(from),
            LocalDateTime.parse(to));
    }
}
}

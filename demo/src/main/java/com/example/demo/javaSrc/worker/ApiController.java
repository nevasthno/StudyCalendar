package com.example.demo.javaSrc.worker;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.web.exchanges.HttpExchange.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @Autowired
    public ApiController(TaskService taskService,
                         EventService eventService,
                         PeopleService peopleService) {
        this.taskService = taskService;
        this.eventService = eventService;
        this.peopleService = peopleService;
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
        return peopleService.getPeopleByRole("teacher");
    }
    
    @PostMapping("/tasks/{id}/toggle-complete")
    public ResponseEntity<Void> toggleTask(@PathVariable Long id) {
        taskService.toggleComplete(id);
        return ResponseEntity.ok().build();
    }
}
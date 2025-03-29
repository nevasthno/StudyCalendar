package com.example.demo.javaSrc.worker;

import com.example.demo.javaSrc.eventsANDtask.Task;
import com.example.demo.javaSrc.eventsANDtask.TaskService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
         this.taskService = taskService;
    }

    @GetMapping
    public List<Task> getAllTasks() {
         return taskService.getAllTasks();
    }

    @PostMapping
    public Task createTask(@RequestBody Task task) {
         return taskService.createTask(task);
    }

}

package com.example.demo.javaSrc.eventsANDtask;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public Task updateTask(Long taskId, Task updatedTask) {
        // Ищем задачу по ID
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isPresent()) {
            Task existingTask = optionalTask.get();
            existingTask.setTitle(updatedTask.getTitle());
            existingTask.setContent(updatedTask.getContent());
            existingTask.setDeadline(updatedTask.getDeadline());
            return taskRepository.save(existingTask);
        } else {
            throw new RuntimeException("Task with ID " + taskId + " not found.");
        }
    }

    /**
     * Удалить задачу по ID
     */
    public void deleteTask(Long taskId) {
        if (taskRepository.existsById(taskId)) {
            taskRepository.deleteById(taskId);
        } else {
            throw new RuntimeException("Task with ID " + taskId + " not found.");
        }
    }

    /**
     * Получить одну задачу по ID
     */
    public Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task with ID " + taskId + " not found."));
    }

    public void markTaskAsCompleted(Long userId, Long taskId) {
        if (taskRepository.existsById(taskId)) {
            taskRepository.markTaskAsCompleted(userId, taskId);
        } else {
            throw new RuntimeException("Task with ID " + taskId + " not found.");
        }
    }
}

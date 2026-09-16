package com.technizer.taskapi.task;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> listForOwner(Long ownerId) {
        return taskRepository.findByOwnerId(ownerId);
    }

    public List<Task> listCompletedForOwner(Long ownerId) {
        return taskRepository.findByOwnerIdAndCompletedTrue(ownerId);
    }

    public Task create(Long ownerId, String title, String description) {
        Task task = new Task(title, description, ownerId);
        return taskRepository.save(task);
    }

    public Task update(Long ownerId, Long taskId, String title, String description) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("Task not found: " + taskId));

        if (!task.getOwnerId().equals(ownerId)) {
            throw new NoSuchElementException("Task not found: " + taskId);
        }

        task.setTitle(title);
        task.setDescription(description);
        return taskRepository.save(task);
    }

    public Task markCompleted(Long ownerId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("Task not found: " + taskId));

        // NOTE: no ownership check here yet - flagged as a TODO for the
        // security-review demo (a user can complete another user's task).
        task.setCompleted(true);
        return taskRepository.save(task);
    }

    public void delete(Long ownerId, Long taskId) {
        taskRepository.deleteById(taskId);
    }
}

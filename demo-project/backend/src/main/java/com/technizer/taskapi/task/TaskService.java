package com.technizer.taskapi.task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

  // Explicit rank rather than enum ordinal, so a future reordering of the Priority
  // constants can't silently change sort behavior (see specs/001-task-priority/research.md).
  private static final Comparator<Task> HIGH_TO_LOW =
      Comparator.comparingInt(
          task ->
              switch (task.getPriority()) {
                case HIGH -> 0;
                case MEDIUM -> 1;
                case LOW -> 2;
              });

  private final TaskRepository taskRepository;

  public TaskService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  public List<Task> listForOwner(Long ownerId) {
    return taskRepository.findByOwnerId(ownerId);
  }

  public List<Task> listForOwner(
      Long ownerId, List<Priority> priorities, Boolean completed, boolean sortByPriority) {
    List<Task> tasks = taskRepository.findByOwnerIdWithFilters(ownerId, priorities, completed);
    if (sortByPriority) {
      tasks = new ArrayList<>(tasks);
      tasks.sort(HIGH_TO_LOW);
    }
    return tasks;
  }

  public PagedTasks listForOwner(
      Long ownerId,
      List<Priority> priorities,
      Boolean completed,
      boolean sortByPriority,
      int page,
      int size) {
    List<Task> matching = listForOwner(ownerId, priorities, completed, sortByPriority);
    int totalElements = matching.size();
    int totalPages = (totalElements + size - 1) / size;
    int fromIndex = (int) Math.min((long) page * size, totalElements);
    int toIndex = (int) Math.min((long) fromIndex + size, totalElements);
    return new PagedTasks(
        matching.subList(fromIndex, toIndex), page, size, totalElements, totalPages);
  }

  public List<Task> listCompletedForOwner(Long ownerId) {
    return taskRepository.findByOwnerIdAndCompletedTrue(ownerId);
  }

  public Task create(Long ownerId, String title, String description, LocalDate dueDate) {
    Task task = new Task(title, description, ownerId);
    task.setDueDate(dueDate);
    return taskRepository.save(task);
  }

  public Task update(
      Long ownerId, Long taskId, String title, String description, LocalDate dueDate) {
    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new NoSuchElementException("Task not found: " + taskId));

    if (!task.getOwnerId().equals(ownerId)) {
      throw new NoSuchElementException("Task not found: " + taskId);
    }

    task.setTitle(title);
    task.setDescription(description);
    task.setDueDate(dueDate);
    return taskRepository.save(task);
  }

  public Task markCompleted(Long ownerId, Long taskId) {
    Task task =
        taskRepository
            .findById(taskId)
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

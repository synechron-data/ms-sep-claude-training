package com.technizer.taskapi.task;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

  private final TaskService taskService;

  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }

  // Return type is intentionally Object: a bare List<Task> (unchanged shape) when no
  // page/size is sent, or a PagedTasks wrapper when pagination is requested.
  @GetMapping
  public Object list(
      @AuthenticationPrincipal Long ownerId,
      @RequestParam(required = false) List<Priority> priority,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    Boolean completed = parseStatus(status);
    boolean sortByPriority = parseSort(sort);

    if (page == null && size == null) {
      return taskService.listForOwner(ownerId, priority, completed, sortByPriority);
    }

    int resolvedPage = page != null ? page : 0;
    int resolvedSize = size != null ? size : 20;
    if (resolvedPage < 0 || resolvedSize < 1) {
      throw new IllegalArgumentException("page must be >= 0 and size must be >= 1");
    }
    return taskService.listForOwner(
        ownerId, priority, completed, sortByPriority, resolvedPage, resolvedSize);
  }

  private Boolean parseStatus(String status) {
    if (status == null) {
      return null;
    }
    return switch (status) {
      case "complete" -> true;
      case "incomplete" -> false;
      default -> throw new IllegalArgumentException("status must be 'complete' or 'incomplete'");
    };
  }

  private boolean parseSort(String sort) {
    if (sort == null) {
      return false;
    }
    if (!sort.equals("priority")) {
      throw new IllegalArgumentException("sort must be 'priority'");
    }
    return true;
  }

  @GetMapping("/completed")
  public List<Task> listCompleted(@AuthenticationPrincipal Long ownerId) {
    return taskService.listCompletedForOwner(ownerId);
  }

  @PostMapping
  public Task create(
      @AuthenticationPrincipal Long ownerId, @RequestBody CreateTaskRequest request) {
    return taskService.create(ownerId, request.title(), request.description(), request.dueDate());
  }

  @PutMapping("/{id}")
  public Task update(
      @AuthenticationPrincipal Long ownerId,
      @PathVariable Long id,
      @RequestBody UpdateTaskRequest request) {
    return taskService.update(
        ownerId, id, request.title(), request.description(), request.dueDate());
  }

  @PatchMapping("/{id}/complete")
  public Task complete(@AuthenticationPrincipal Long ownerId, @PathVariable Long id) {
    return taskService.markCompleted(ownerId, id);
  }

  @DeleteMapping("/{id}")
  public void delete(@AuthenticationPrincipal Long ownerId, @PathVariable Long id) {
    taskService.delete(ownerId, id);
  }

  public record CreateTaskRequest(@NotBlank String title, String description, LocalDate dueDate) {}

  public record UpdateTaskRequest(@NotBlank String title, String description, LocalDate dueDate) {}
}

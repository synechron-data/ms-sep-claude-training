package com.technizer.taskapi.task;

// TRAINING NOTE: This file is intentionally left with only a placeholder.
// In Module 6 (Skills) and Module 9-10 (Spec-Driven Development / Spec Kit),
// participants will use Claude Code to generate real test coverage for
// TaskService, including the missing ownership check on markCompleted()
// and delete() surfaced during the Module 13 security-review demo.

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskServiceTest {

  private TaskRepository taskRepository;
  private TaskService taskService;

  @BeforeEach
  void setUp() {
    taskRepository = mock(TaskRepository.class);
    taskService = new TaskService(taskRepository);
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void placeholder() {
    // Replaced live during the session.
  }

  @Test
  void updateChangesTitleAndDescriptionForOwner() {
    Task existing = new Task("Old title", "Old description", 1L);
    existing.setId(10L);
    when(taskRepository.findById(10L)).thenReturn(Optional.of(existing));

    Task result = taskService.update(1L, 10L, "New title", "New description", null);

    assertThat(result.getTitle()).isEqualTo("New title");
    assertThat(result.getDescription()).isEqualTo("New description");
  }

  @Test
  void updateThrowsWhenTaskNotFound() {
    when(taskRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> taskService.update(1L, 99L, "New title", "New description", null))
        .isInstanceOf(NoSuchElementException.class);
  }

  @Test
  void listCompletedForOwnerReturnsOnlyCompletedTasksFromRepository() {
    Task completed = new Task("Completed task", "Description", 1L);
    completed.setId(10L);
    completed.setCompleted(true);
    when(taskRepository.findByOwnerIdAndCompletedTrue(1L)).thenReturn(List.of(completed));

    List<Task> result = taskService.listCompletedForOwner(1L);

    assertThat(result).containsExactly(completed);
  }

  @Test
  void updateThrowsWhenCallerDoesNotOwnTask() {
    Task existing = new Task("Old title", "Old description", 2L);
    existing.setId(10L);
    when(taskRepository.findById(10L)).thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> taskService.update(1L, 10L, "New title", "New description", null))
        .isInstanceOf(NoSuchElementException.class);
  }

  @Test
  void createPersistsDueDate() {
    LocalDate dueDate = LocalDate.of(2026, 1, 1);

    Task result = taskService.create(1L, "Title", "Description", dueDate);

    assertThat(result.getDueDate()).isEqualTo(dueDate);
  }

  @Test
  void updatePersistsChangedDueDate() {
    Task existing = new Task("Old title", "Old description", 1L);
    existing.setId(10L);
    existing.setDueDate(LocalDate.of(2026, 1, 1));
    when(taskRepository.findById(10L)).thenReturn(Optional.of(existing));

    LocalDate newDueDate = LocalDate.of(2026, 2, 1);
    Task result = taskService.update(1L, 10L, "New title", "New description", newDueDate);

    assertThat(result.getDueDate()).isEqualTo(newDueDate);
  }

  @Test
  void isOverdueTrueWhenDueDateInPastAndIncomplete() {
    Task task = new Task("Title", "Description", 1L);
    task.setDueDate(LocalDate.now().minusDays(1));

    assertThat(task.isOverdue()).isTrue();
  }

  @Test
  void isOverdueFalseWhenCompleted() {
    Task task = new Task("Title", "Description", 1L);
    task.setDueDate(LocalDate.now().minusDays(1));
    task.setCompleted(true);

    assertThat(task.isOverdue()).isFalse();
  }

  @Test
  void isOverdueFalseWhenDueDateIsNull() {
    Task task = new Task("Title", "Description", 1L);

    assertThat(task.isOverdue()).isFalse();
  }

  @Test
  void isOverdueFalseWhenDueDateInFuture() {
    Task task = new Task("Title", "Description", 1L);
    task.setDueDate(LocalDate.now().plusDays(1));

    assertThat(task.isOverdue()).isFalse();
  }

  @Test
  void listForOwnerWithNoFiltersDelegatesWithNullsAndPreservesOrder() {
    Task first = new Task("First", "d", 1L);
    Task second = new Task("Second", "d", 1L);
    when(taskRepository.findByOwnerIdWithFilters(1L, null, null))
        .thenReturn(List.of(first, second));

    List<Task> result = taskService.listForOwner(1L, null, null, false);

    assertThat(result).containsExactly(first, second);
    verify(taskRepository).findByOwnerIdWithFilters(eq(1L), isNull(), isNull());
  }

  @Test
  void listForOwnerPassesPriorityAndCompletedFiltersToRepository() {
    List<Priority> priorities = List.of(Priority.HIGH, Priority.MEDIUM);
    when(taskRepository.findByOwnerIdWithFilters(1L, priorities, true)).thenReturn(List.of());

    taskService.listForOwner(1L, priorities, true, false);

    verify(taskRepository).findByOwnerIdWithFilters(1L, priorities, true);
  }

  @Test
  void listForOwnerSortsHighToLowWhenRequested() {
    Task low = new Task("Low", "d", 1L);
    low.setPriority(Priority.LOW);
    Task high = new Task("High", "d", 1L);
    high.setPriority(Priority.HIGH);
    Task medium = new Task("Medium", "d", 1L);
    medium.setPriority(Priority.MEDIUM);
    when(taskRepository.findByOwnerIdWithFilters(1L, null, null))
        .thenReturn(List.of(low, high, medium));

    List<Task> result = taskService.listForOwner(1L, null, null, true);

    assertThat(result).containsExactly(high, medium, low);
  }

  @Test
  void listForOwnerSortKeepsStableOrderForSamePriority() {
    Task firstHigh = new Task("First high", "d", 1L);
    firstHigh.setPriority(Priority.HIGH);
    Task secondHigh = new Task("Second high", "d", 1L);
    secondHigh.setPriority(Priority.HIGH);
    when(taskRepository.findByOwnerIdWithFilters(1L, null, null))
        .thenReturn(List.of(firstHigh, secondHigh));

    List<Task> result = taskService.listForOwner(1L, null, null, true);

    assertThat(result).containsExactly(firstHigh, secondHigh);
  }

  @Test
  void listForOwnerDoesNotSortWhenNotRequested() {
    Task low = new Task("Low", "d", 1L);
    low.setPriority(Priority.LOW);
    Task high = new Task("High", "d", 1L);
    high.setPriority(Priority.HIGH);
    when(taskRepository.findByOwnerIdWithFilters(1L, null, null)).thenReturn(List.of(low, high));

    List<Task> result = taskService.listForOwner(1L, null, null, false);

    assertThat(result).containsExactly(low, high);
  }

  @Test
  void listForOwnerPagedSlicesResultsAndComputesMetadata() {
    List<Task> tasks =
        List.of(
            new Task("1", "d", 1L),
            new Task("2", "d", 1L),
            new Task("3", "d", 1L),
            new Task("4", "d", 1L),
            new Task("5", "d", 1L));
    when(taskRepository.findByOwnerIdWithFilters(1L, null, null)).thenReturn(tasks);

    PagedTasks result = taskService.listForOwner(1L, null, null, false, 1, 2);

    assertThat(result.items()).containsExactly(tasks.get(2), tasks.get(3));
    assertThat(result.page()).isEqualTo(1);
    assertThat(result.size()).isEqualTo(2);
    assertThat(result.totalElements()).isEqualTo(5);
    assertThat(result.totalPages()).isEqualTo(3);
  }

  @Test
  void listForOwnerPagedPastEndReturnsEmptyItemsWithCorrectMetadata() {
    List<Task> tasks = List.of(new Task("1", "d", 1L), new Task("2", "d", 1L));
    when(taskRepository.findByOwnerIdWithFilters(1L, null, null)).thenReturn(tasks);

    PagedTasks result = taskService.listForOwner(1L, null, null, false, 5, 20);

    assertThat(result.items()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(2);
    assertThat(result.totalPages()).isEqualTo(1);
  }

  @Test
  void listForOwnerPagedAppliesSortBeforeSlicing() {
    Task low = new Task("Low", "d", 1L);
    low.setPriority(Priority.LOW);
    Task high = new Task("High", "d", 1L);
    high.setPriority(Priority.HIGH);
    Task medium = new Task("Medium", "d", 1L);
    medium.setPriority(Priority.MEDIUM);
    when(taskRepository.findByOwnerIdWithFilters(1L, null, null))
        .thenReturn(List.of(low, high, medium));

    PagedTasks result = taskService.listForOwner(1L, null, null, true, 0, 2);

    assertThat(result.items()).containsExactly(high, medium);
  }
}

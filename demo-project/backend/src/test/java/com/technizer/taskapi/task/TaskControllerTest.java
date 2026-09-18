package com.technizer.taskapi.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskControllerTest {

  private TaskService taskService;
  private TaskController taskController;

  @BeforeEach
  void setUp() {
    taskService = mock(TaskService.class);
    taskController = new TaskController(taskService);
  }

  @Test
  void listCompletedReturnsCompletedTasksForOwner() {
    Task completed = new Task("Completed task", "Description", 1L);
    completed.setId(10L);
    completed.setCompleted(true);
    when(taskService.listCompletedForOwner(1L)).thenReturn(List.of(completed));

    List<Task> result = taskController.listCompleted(1L);

    assertThat(result).containsExactly(completed);
  }

  @Test
  void listCompletedReturnsEmptyListWhenOwnerHasNoCompletedTasks() {
    when(taskService.listCompletedForOwner(1L)).thenReturn(Collections.emptyList());

    List<Task> result = taskController.listCompleted(1L);

    assertThat(result).isEmpty();
  }

  @Test
  void createPassesDueDateFromRequestToService() {
    LocalDate dueDate = LocalDate.of(2026, 1, 1);
    Task created = new Task("Title", "Description", 1L);
    created.setDueDate(dueDate);
    when(taskService.create(1L, "Title", "Description", dueDate)).thenReturn(created);

    Task result =
        taskController.create(
            1L, new TaskController.CreateTaskRequest("Title", "Description", dueDate));

    assertThat(result.getDueDate()).isEqualTo(dueDate);
    verify(taskService).create(eq(1L), eq("Title"), eq("Description"), eq(dueDate));
  }

  @Test
  void updatePassesDueDateFromRequestToService() {
    LocalDate dueDate = LocalDate.of(2026, 2, 1);
    Task updated = new Task("Title", "Description", 1L);
    updated.setDueDate(dueDate);
    when(taskService.update(1L, 10L, "Title", "Description", dueDate)).thenReturn(updated);

    Task result =
        taskController.update(
            1L, 10L, new TaskController.UpdateTaskRequest("Title", "Description", dueDate));

    assertThat(result.getDueDate()).isEqualTo(dueDate);
    verify(taskService).update(eq(1L), eq(10L), eq("Title"), eq("Description"), eq(dueDate));
  }

  @Test
  void listWithNoParamsReturnsPlainListFromService() {
    List<Task> tasks = List.of(new Task("Title", "Description", 1L));
    when(taskService.listForOwner(1L, null, null, false)).thenReturn(tasks);

    Object result = taskController.list(1L, null, null, null, null, null);

    assertThat(result).isEqualTo(tasks);
    verify(taskService).listForOwner(eq(1L), isNull(), isNull(), eq(false));
  }

  @Test
  void listPassesPriorityListThroughToService() {
    List<Priority> priorities = List.of(Priority.HIGH, Priority.LOW);
    when(taskService.listForOwner(1L, priorities, null, false)).thenReturn(List.of());

    taskController.list(1L, priorities, null, null, null, null);

    verify(taskService).listForOwner(eq(1L), eq(priorities), isNull(), eq(false));
  }

  @Test
  void listWithStatusCompleteMapsToTrue() {
    when(taskService.listForOwner(1L, null, true, false)).thenReturn(List.of());

    taskController.list(1L, null, "complete", null, null, null);

    verify(taskService).listForOwner(eq(1L), isNull(), eq(true), eq(false));
  }

  @Test
  void listWithStatusIncompleteMapsToFalse() {
    when(taskService.listForOwner(1L, null, false, false)).thenReturn(List.of());

    taskController.list(1L, null, "incomplete", null, null, null);

    verify(taskService).listForOwner(eq(1L), isNull(), eq(false), eq(false));
  }

  @Test
  void listWithInvalidStatusThrowsIllegalArgumentException() {
    assertThatThrownBy(() -> taskController.list(1L, null, "bogus", null, null, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void listWithSortPriorityPassesTrueToService() {
    when(taskService.listForOwner(1L, null, null, true)).thenReturn(List.of());

    taskController.list(1L, null, null, "priority", null, null);

    verify(taskService).listForOwner(eq(1L), isNull(), isNull(), eq(true));
  }

  @Test
  void listWithInvalidSortThrowsIllegalArgumentException() {
    assertThatThrownBy(() -> taskController.list(1L, null, null, "bogus", null, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void listWithPageOnlyDefaultsSizeTo20() {
    when(taskService.listForOwner(1L, null, null, false, 0, 20))
        .thenReturn(new PagedTasks(List.of(), 0, 20, 0, 0));

    taskController.list(1L, null, null, null, 0, null);

    verify(taskService).listForOwner(eq(1L), isNull(), isNull(), eq(false), eq(0), eq(20));
  }

  @Test
  void listWithSizeOnlyDefaultsPageTo0() {
    when(taskService.listForOwner(1L, null, null, false, 0, 5))
        .thenReturn(new PagedTasks(List.of(), 0, 5, 0, 0));

    taskController.list(1L, null, null, null, null, 5);

    verify(taskService).listForOwner(eq(1L), isNull(), isNull(), eq(false), eq(0), eq(5));
  }

  @Test
  void listWithPageAndSizeReturnsPagedTasksFromService() {
    PagedTasks paged = new PagedTasks(List.of(), 2, 3, 10, 4);
    when(taskService.listForOwner(1L, null, null, false, 2, 3)).thenReturn(paged);

    Object result = taskController.list(1L, null, null, null, 2, 3);

    assertThat(result).isEqualTo(paged);
  }

  @Test
  void listWithNegativePageThrowsIllegalArgumentException() {
    assertThatThrownBy(() -> taskController.list(1L, null, null, null, -1, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void listWithZeroSizeThrowsIllegalArgumentException() {
    assertThatThrownBy(() -> taskController.list(1L, null, null, null, null, 0))
        .isInstanceOf(IllegalArgumentException.class);
  }
}

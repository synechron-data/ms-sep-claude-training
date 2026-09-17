package com.technizer.taskapi.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        Task result = taskController.create(1L, new TaskController.CreateTaskRequest("Title", "Description", dueDate));

        assertThat(result.getDueDate()).isEqualTo(dueDate);
        verify(taskService).create(eq(1L), eq("Title"), eq("Description"), eq(dueDate));
    }

    @Test
    void updatePassesDueDateFromRequestToService() {
        LocalDate dueDate = LocalDate.of(2026, 2, 1);
        Task updated = new Task("Title", "Description", 1L);
        updated.setDueDate(dueDate);
        when(taskService.update(1L, 10L, "Title", "Description", dueDate)).thenReturn(updated);

        Task result = taskController.update(1L, 10L, new TaskController.UpdateTaskRequest("Title", "Description", dueDate));

        assertThat(result.getDueDate()).isEqualTo(dueDate);
        verify(taskService).update(eq(1L), eq(10L), eq("Title"), eq("Description"), eq(dueDate));
    }
}

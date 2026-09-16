package com.technizer.taskapi.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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
}

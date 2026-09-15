package com.technizer.taskapi.task;

// TRAINING NOTE: This file is intentionally left with only a placeholder.
// In Module 6 (Skills) and Module 9-10 (Spec-Driven Development / Spec Kit),
// participants will use Claude Code to generate real test coverage for
// TaskService, including the missing ownership check on markCompleted()
// and delete() surfaced during the Module 13 security-review demo.

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

        Task result = taskService.update(1L, 10L, "New title", "New description");

        assertThat(result.getTitle()).isEqualTo("New title");
        assertThat(result.getDescription()).isEqualTo("New description");
    }

    @Test
    void updateThrowsWhenTaskNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.update(1L, 99L, "New title", "New description"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void updateThrowsWhenCallerDoesNotOwnTask() {
        Task existing = new Task("Old title", "Old description", 2L);
        existing.setId(10L);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> taskService.update(1L, 10L, "New title", "New description"))
                .isInstanceOf(NoSuchElementException.class);
    }
}

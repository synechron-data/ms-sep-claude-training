package com.technizer.taskapi.task;

import java.util.List;

public record PagedTasks(
    List<Task> items, int page, int size, long totalElements, int totalPages) {}

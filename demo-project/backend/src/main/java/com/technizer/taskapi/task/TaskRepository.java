package com.technizer.taskapi.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByOwnerId(Long ownerId);

    List<Task> findByOwnerIdAndCompletedTrue(Long ownerId);
}

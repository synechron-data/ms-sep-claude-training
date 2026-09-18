package com.technizer.taskapi.task;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

  List<Task> findByOwnerId(Long ownerId);

  List<Task> findByOwnerIdAndCompletedTrue(Long ownerId);

  // priorities/completed are matched only when non-null, so a caller that omits both filters
  // gets exactly the same rows (and order) as findByOwnerId.
  @Query(
      "SELECT t FROM Task t WHERE t.ownerId = :ownerId "
          + "AND (:priorities IS NULL OR t.priority IN :priorities) "
          + "AND (:completed IS NULL OR t.completed = :completed)")
  List<Task> findByOwnerIdWithFilters(
      @Param("ownerId") Long ownerId,
      @Param("priorities") List<Priority> priorities,
      @Param("completed") Boolean completed);
}

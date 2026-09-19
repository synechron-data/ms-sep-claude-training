package com.technizer.taskapi.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.technizer.taskapi.auth.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskDeleteOwnershipIntegrationTest {

  private static final Long USER_A = 1001L;
  private static final Long USER_B = 1002L;

  @Autowired private MockMvc mockMvc;
  @Autowired private TaskRepository taskRepository;
  @Autowired private JwtUtil jwtUtil;

  private String bearer(Long userId) {
    return "Bearer " + jwtUtil.generateToken(userId, "user" + userId + "@example.com");
  }

  @Test
  void otherUserCannotDeleteTaskAndTaskSurvives() throws Exception {
    Task task = taskRepository.save(new Task("A's task", "private", USER_A));

    mockMvc
        .perform(delete("/api/tasks/" + task.getId()).header("Authorization", bearer(USER_B)))
        .andExpect(status().isNotFound());

    assertThat(taskRepository.findById(task.getId())).isPresent();
  }

  @Test
  void ownerCanDeleteOwnTask() throws Exception {
    Task task = taskRepository.save(new Task("A's task", "private", USER_A));

    mockMvc
        .perform(delete("/api/tasks/" + task.getId()).header("Authorization", bearer(USER_A)))
        .andExpect(status().isOk());

    assertThat(taskRepository.findById(task.getId())).isEmpty();
  }

  @Test
  void nonOwnerAndNonexistentIdProduceIdenticalResponses() throws Exception {
    Task task = taskRepository.save(new Task("A's task", "private", USER_A));

    String foreignBody =
        mockMvc
            .perform(delete("/api/tasks/" + task.getId()).header("Authorization", bearer(USER_B)))
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse()
            .getContentAsString();

    mockMvc
        .perform(
            delete("/api/tasks/" + (task.getId() + 100_000))
                .header("Authorization", bearer(USER_B)))
        .andExpect(status().isNotFound())
        .andExpect(content().json(foreignBody, true));
  }

  @Test
  void unauthenticatedDeleteIsRejected() throws Exception {
    Task task = taskRepository.save(new Task("A's task", "private", USER_A));

    mockMvc.perform(delete("/api/tasks/" + task.getId())).andExpect(status().is4xxClientError());

    assertThat(taskRepository.findById(task.getId())).isPresent();
  }
}

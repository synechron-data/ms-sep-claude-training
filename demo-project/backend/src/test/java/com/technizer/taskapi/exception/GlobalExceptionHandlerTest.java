package com.technizer.taskapi.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  // Used only to obtain a real MethodParameter for building a MethodArgumentTypeMismatchException.
  private void dummy(String value) {}

  @Test
  void handleBadRequestReturns400WithMessage() {
    ResponseEntity<Map<String, String>> response =
        handler.handleBadRequest(new IllegalArgumentException("bad"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).containsEntry("error", "bad");
  }

  @Test
  void handleNotFoundReturns404() {
    ResponseEntity<Map<String, String>> response =
        handler.handleNotFound(new NoSuchElementException("missing"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).containsEntry("error", "missing");
  }

  @Test
  void handleTypeMismatchReturns400NotGeneric500() throws NoSuchMethodException {
    Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("dummy", String.class);
    MethodParameter param = new MethodParameter(method, 0);
    MethodArgumentTypeMismatchException ex =
        new MethodArgumentTypeMismatchException(
            "WRONG", String.class, "priority", param, new IllegalArgumentException());

    ResponseEntity<Map<String, String>> response = handler.handleTypeMismatch(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().get("error")).contains("priority");
  }

  @Test
  void handleGenericReturns500WithoutLeakingDetails() {
    ResponseEntity<Map<String, String>> response =
        handler.handleGeneric(new RuntimeException("internal detail"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody().get("error")).doesNotContain("internal detail");
  }
}

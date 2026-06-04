package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class ErrorResourceSerializerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void should_serialize_single_field_error() throws JsonProcessingException {
    FieldErrorResource fieldError =
        new FieldErrorResource("object", "email", "NotBlank", "can't be empty");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(fieldError));

    String json = objectMapper.writeValueAsString(errorResource);

    assertNotNull(json);
    assertTrue(json.contains("errors"));
    assertTrue(json.contains("email"));
    assertTrue(json.contains("can't be empty"));
  }

  @Test
  void should_serialize_multiple_field_errors() throws JsonProcessingException {
    FieldErrorResource error1 =
        new FieldErrorResource("object", "email", "NotBlank", "can't be empty");
    FieldErrorResource error2 =
        new FieldErrorResource("object", "username", "NotBlank", "is required");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(error1, error2));

    String json = objectMapper.writeValueAsString(errorResource);

    assertNotNull(json);
    assertTrue(json.contains("email"));
    assertTrue(json.contains("username"));
  }

  @Test
  void should_serialize_multiple_errors_for_same_field() throws JsonProcessingException {
    FieldErrorResource error1 =
        new FieldErrorResource("object", "password", "NotBlank", "can't be empty");
    FieldErrorResource error2 = new FieldErrorResource("object", "password", "Size", "too short");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(error1, error2));

    String json = objectMapper.writeValueAsString(errorResource);

    assertNotNull(json);
    assertTrue(json.contains("password"));
    assertTrue(json.contains("can't be empty"));
    assertTrue(json.contains("too short"));
  }

  @Test
  void should_serialize_empty_errors() throws JsonProcessingException {
    ErrorResource errorResource = new ErrorResource(Collections.emptyList());

    String json = objectMapper.writeValueAsString(errorResource);

    assertNotNull(json);
    assertTrue(json.contains("errors"));
  }
}

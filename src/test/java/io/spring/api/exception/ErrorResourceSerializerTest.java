package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ErrorResourceSerializerTest {

  @Test
  void should_serialize_error_resource() throws JsonProcessingException {
    FieldErrorResource field1 = new FieldErrorResource("User", "email", "NotBlank", "can't be empty");
    FieldErrorResource field2 = new FieldErrorResource("User", "username", "NotBlank", "can't be empty");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(field1, field2));

    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(errorResource);

    assertNotNull(json);
    assertTrue(json.contains("errors"));
    assertTrue(json.contains("email"));
    assertTrue(json.contains("username"));
    assertTrue(json.contains("can't be empty"));
  }

  @Test
  void should_serialize_multiple_errors_for_same_field() throws JsonProcessingException {
    FieldErrorResource field1 = new FieldErrorResource("User", "email", "NotBlank", "can't be empty");
    FieldErrorResource field2 = new FieldErrorResource("User", "email", "Email", "invalid format");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(field1, field2));

    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(errorResource);

    assertNotNull(json);
    assertTrue(json.contains("can't be empty"));
    assertTrue(json.contains("invalid format"));
  }
}

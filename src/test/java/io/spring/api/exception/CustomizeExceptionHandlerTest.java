package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

public class CustomizeExceptionHandlerTest {

  @Test
  void should_construct_error_resource() {
    List<FieldErrorResource> fieldErrors =
        Arrays.asList(
            new FieldErrorResource("Article", "title", "NotBlank", "can't be empty"),
            new FieldErrorResource("Article", "body", "NotBlank", "can't be empty"));
    ErrorResource errorResource = new ErrorResource(fieldErrors);

    assertNotNull(errorResource.getFieldErrors());
    assertEquals(2, errorResource.getFieldErrors().size());
  }

  @Test
  void should_serialize_error_resource() throws Exception {
    List<FieldErrorResource> fieldErrors =
        Arrays.asList(
            new FieldErrorResource("Article", "title", "NotBlank", "can't be empty"),
            new FieldErrorResource("Article", "title", "Size", "too short"));
    ErrorResource errorResource = new ErrorResource(fieldErrors);

    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(errorResource);
    assertNotNull(json);
    assertTrue(json.contains("errors"));
    assertTrue(json.contains("title"));
    assertTrue(json.contains("can't be empty"));
    assertTrue(json.contains("too short"));
  }

  @Test
  void should_serialize_single_field_error() throws Exception {
    List<FieldErrorResource> fieldErrors =
        Arrays.asList(new FieldErrorResource("User", "email", "Email", "should be an email"));
    ErrorResource errorResource = new ErrorResource(fieldErrors);

    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(errorResource);
    assertNotNull(json);
    assertTrue(json.contains("email"));
    assertTrue(json.contains("should be an email"));
  }

  @Test
  void should_construct_field_error_resource() {
    FieldErrorResource fer = new FieldErrorResource("User", "email", "Email", "invalid");
    assertEquals("User", fer.getResource());
    assertEquals("email", fer.getField());
    assertEquals("Email", fer.getCode());
    assertEquals("invalid", fer.getMessage());
  }

  @Test
  void should_construct_invalid_request_exception() {
    org.springframework.validation.MapBindingResult errors =
        new org.springframework.validation.MapBindingResult(new java.util.HashMap<>(), "object");
    errors.rejectValue("field", "code", "message");
    InvalidRequestException ex = new InvalidRequestException(errors);
    assertNotNull(ex.getErrors());
    assertEquals(1, ex.getErrors().getFieldErrors().size());
  }

  @Test
  void should_construct_resource_not_found_exception() {
    ResourceNotFoundException ex = new ResourceNotFoundException();
    assertNotNull(ex);
  }

  @Test
  void should_construct_no_authorization_exception() {
    NoAuthorizationException ex = new NoAuthorizationException();
    assertNotNull(ex);
  }

  @Test
  void should_construct_invalid_authentication_exception() {
    InvalidAuthenticationException ex = new InvalidAuthenticationException();
    assertEquals("invalid email or password", ex.getMessage());
  }

  @Test
  void should_handle_invalid_request_exception() {
    CustomizeExceptionHandler handler = new CustomizeExceptionHandler();
    org.springframework.validation.MapBindingResult errors =
        new org.springframework.validation.MapBindingResult(new java.util.HashMap<>(), "article");
    errors.rejectValue("title", "NotBlank", "can't be empty");
    InvalidRequestException ex = new InvalidRequestException(errors);
    WebRequest request = mock(WebRequest.class);

    ResponseEntity<Object> response = handler.handleInvalidRequest(ex, request);
    assertNotNull(response);
    assertEquals(422, response.getStatusCodeValue());
  }

  @Test
  void should_handle_invalid_authentication_exception_response() {
    CustomizeExceptionHandler handler = new CustomizeExceptionHandler();
    InvalidAuthenticationException ex = new InvalidAuthenticationException();
    WebRequest request = mock(WebRequest.class);

    ResponseEntity<Object> response = handler.handleInvalidAuthentication(ex, request);
    assertNotNull(response);
    assertEquals(422, response.getStatusCodeValue());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void should_handle_constraint_violation_exception() {
    CustomizeExceptionHandler handler = new CustomizeExceptionHandler();
    javax.validation.ConstraintViolation violation =
        mock(javax.validation.ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn(String.class);
    javax.validation.Path path = mock(javax.validation.Path.class);
    when(path.toString()).thenReturn("createUser.param.email");
    when(violation.getPropertyPath()).thenReturn(path);
    javax.validation.metadata.ConstraintDescriptor descriptor =
        mock(javax.validation.metadata.ConstraintDescriptor.class);
    java.lang.annotation.Annotation annotation =
        new javax.validation.constraints.Email() {
          @Override
          public String message() {
            return "invalid";
          }

          @Override
          public Class<?>[] groups() {
            return new Class[0];
          }

          @Override
          public Class<? extends javax.validation.Payload>[] payload() {
            return new Class[0];
          }

          @Override
          public String regexp() {
            return ".*";
          }

          @Override
          public javax.validation.constraints.Pattern.Flag[] flags() {
            return new javax.validation.constraints.Pattern.Flag[0];
          }

          @Override
          public Class<? extends java.lang.annotation.Annotation> annotationType() {
            return javax.validation.constraints.Email.class;
          }
        };
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(violation.getMessage()).thenReturn("invalid email");

    java.util.Set violations = new java.util.HashSet();
    violations.add(violation);
    javax.validation.ConstraintViolationException cve =
        new javax.validation.ConstraintViolationException("error", violations);
    WebRequest request = mock(WebRequest.class);

    ErrorResource result = handler.handleConstraintViolation(cve, request);
    assertNotNull(result);
    assertEquals(1, result.getFieldErrors().size());
    assertEquals("email", result.getFieldErrors().get(0).getField());
  }
}

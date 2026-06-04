package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

public class CustomizeExceptionHandlerTest {

  private CustomizeExceptionHandler handler;
  private WebRequest webRequest;

  @BeforeEach
  void setUp() {
    handler = new CustomizeExceptionHandler();
    webRequest = new ServletWebRequest(new MockHttpServletRequest());
  }

  @Test
  void should_handle_invalid_request_exception() {
    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(new Object(), "testObject");
    bindingResult.addError(new FieldError("testObject", "field1", "must not be null"));
    bindingResult.addError(new FieldError("testObject", "field2", "must not be empty"));
    InvalidRequestException exception = new InvalidRequestException(bindingResult);

    ResponseEntity<Object> response = handler.handleInvalidRequest(exception, webRequest);

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody() instanceof ErrorResource);
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();

    ResponseEntity<Object> response = handler.handleInvalidAuthentication(exception, webRequest);

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody() instanceof Map);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertNotNull(body.get("message"));
  }

  @Test
  void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createViolation("createUser.param.email", "email already exist"));
    ConstraintViolationException ex = new ConstraintViolationException(violations);

    ErrorResource response = handler.handleConstraintViolation(ex, webRequest);

    assertNotNull(response);
  }

  @Test
  void should_handle_constraint_violation_with_simple_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createViolation("email", "must not be blank"));
    ConstraintViolationException ex = new ConstraintViolationException(violations);

    ErrorResource response = handler.handleConstraintViolation(ex, webRequest);

    assertNotNull(response);
  }

  @Test
  void should_handle_constraint_violation_with_multiple_violations() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createViolation("createUser.param.email", "email already exist"));
    violations.add(createViolation("createUser.param.username", "username already exist"));
    ConstraintViolationException ex = new ConstraintViolationException(violations);

    ErrorResource response = handler.handleConstraintViolation(ex, webRequest);

    assertNotNull(response);
  }

  private ConstraintViolation<?> createViolation(String pathStr, String message) {
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn(pathStr);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn(message);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);

    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    return violation;
  }
}

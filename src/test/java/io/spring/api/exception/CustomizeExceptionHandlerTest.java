package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.constraints.NotBlank;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.WebRequest;

public class CustomizeExceptionHandlerTest {

  private CustomizeExceptionHandler handler;
  private WebRequest webRequest;

  @BeforeEach
  void setUp() {
    handler = new CustomizeExceptionHandler();
    webRequest = mock(WebRequest.class);
  }

  @Test
  void should_handle_invalid_request_exception() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "test");
    errors.addError(new FieldError("test", "email", "can't be empty"));
    InvalidRequestException ex = new InvalidRequestException(errors);

    ResponseEntity<Object> response = handler.handleInvalidRequest(ex, webRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void should_handle_invalid_request_with_multiple_errors() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "test");
    errors.addError(new FieldError("test", "email", "can't be empty"));
    errors.addError(new FieldError("test", "password", "too short"));
    InvalidRequestException ex = new InvalidRequestException(errors);

    ResponseEntity<Object> response = handler.handleInvalidRequest(ex, webRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException ex = new InvalidAuthenticationException();

    ResponseEntity<Object> response = handler.handleInvalidAuthentication(ex, webRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("createArticle.param.title");
    when(violation.getPropertyPath()).thenReturn(path);

    Annotation annotation =
        new NotBlank() {
          @Override
          public String message() {
            return "";
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
          public Class<? extends Annotation> annotationType() {
            return NotBlank.class;
          }
        };

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(violation.getMessage()).thenReturn("can't be empty");
    violations.add(violation);

    ConstraintViolationException ex = new ConstraintViolationException(violations);

    ErrorResource result = handler.handleConstraintViolation(ex, webRequest);

    assertNotNull(result);
    assertFalse(result.getFieldErrors().isEmpty());
    assertEquals(1, result.getFieldErrors().size());
    assertEquals("title", result.getFieldErrors().get(0).getField());
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_handle_constraint_violation_with_simple_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("email");
    when(violation.getPropertyPath()).thenReturn(path);

    Annotation annotation =
        new NotBlank() {
          @Override
          public String message() {
            return "";
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
          public Class<? extends Annotation> annotationType() {
            return NotBlank.class;
          }
        };

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(violation.getMessage()).thenReturn("invalid");
    violations.add(violation);

    ConstraintViolationException ex = new ConstraintViolationException(violations);

    ErrorResource result = handler.handleConstraintViolation(ex, webRequest);

    assertNotNull(result);
    assertEquals("email", result.getFieldErrors().get(0).getField());
  }
}

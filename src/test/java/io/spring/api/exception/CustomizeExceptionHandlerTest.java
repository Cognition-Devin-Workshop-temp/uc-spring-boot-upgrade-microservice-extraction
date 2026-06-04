package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.WebRequest;

@ExtendWith(MockitoExtension.class)
public class CustomizeExceptionHandlerTest {

  private CustomizeExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new CustomizeExceptionHandler();
  }

  @Test
  void should_handle_invalid_request_exception() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "article");
    errors.addError(new FieldError("article", "title", "can't be empty"));
    InvalidRequestException exception = new InvalidRequestException(errors);
    WebRequest request = mock(WebRequest.class);

    ResponseEntity<Object> response = handler.handleInvalidRequest(exception, request);

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    assertNotNull(response.getBody());
    assertInstanceOf(ErrorResource.class, response.getBody());
  }

  @Test
  void should_handle_invalid_request_exception_with_multiple_errors() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "article");
    errors.addError(new FieldError("article", "title", "can't be empty"));
    errors.addError(new FieldError("article", "body", "can't be empty"));
    InvalidRequestException exception = new InvalidRequestException(errors);
    WebRequest request = mock(WebRequest.class);

    ResponseEntity<Object> response = handler.handleInvalidRequest(exception, request);

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    WebRequest request = mock(WebRequest.class);

    ResponseEntity<Object> response = handler.handleInvalidAuthentication(exception, request);

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("createArticle.param.title");
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("can't be empty");
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);

    violations.add(violation);
    ConstraintViolationException ex = new ConstraintViolationException(violations);
    WebRequest request = mock(WebRequest.class);

    ErrorResource result = handler.handleConstraintViolation(ex, request);

    assertNotNull(result);
  }

  @Test
  void should_handle_constraint_violation_with_single_segment_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("title");
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("can't be empty");
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);

    violations.add(violation);
    ConstraintViolationException ex = new ConstraintViolationException(violations);
    WebRequest request = mock(WebRequest.class);

    ErrorResource result = handler.handleConstraintViolation(ex, request);

    assertNotNull(result);
  }

  @Test
  void should_create_invalid_request_exception_with_errors() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "article");
    errors.addError(new FieldError("article", "title", "can't be empty"));

    InvalidRequestException exception = new InvalidRequestException(errors);

    assertEquals(errors, exception.getErrors());
  }

  @Test
  void should_create_field_error_resource() {
    FieldErrorResource resource =
        new FieldErrorResource("Article", "title", "NotBlank", "can't be empty");

    assertEquals("Article", resource.getResource());
    assertEquals("title", resource.getField());
    assertEquals("NotBlank", resource.getCode());
    assertEquals("can't be empty", resource.getMessage());
  }
}

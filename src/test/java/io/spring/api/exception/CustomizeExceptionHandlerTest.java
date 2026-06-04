package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.doReturn;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

class CustomizeExceptionHandlerTest {

  private final CustomizeExceptionHandler handler = new CustomizeExceptionHandler();

  @Test
  void should_handle_invalid_request_exception() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult("target", "target");
    errors.addError(new FieldError("target", "title", "can't be empty"));

    InvalidRequestException ire = new InvalidRequestException(errors);
    WebRequest request = mock(WebRequest.class);

    ResponseEntity<Object> response = handler.handleInvalidRequest(ire, request);

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    WebRequest request = mock(WebRequest.class);

    ResponseEntity<Object> response = handler.handleInvalidAuthentication(exception, request);

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    assertNotNull(response.getBody());
    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertEquals("invalid email or password", body.get("message"));
  }

  @Test
  void should_handle_method_argument_not_valid() throws Exception {
    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult("target", "target");
    bindingResult.addError(new FieldError("target", "email", "invalid email"));

    MethodArgumentNotValidException exception =
        new MethodArgumentNotValidException(null, bindingResult);

    WebRequest request = mock(WebRequest.class);

    ResponseEntity<Object> response =
        handler.handleMethodArgumentNotValid(
            exception, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_handle_constraint_violation() {
    ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) Object.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("createUser.param.email");
    when(violation.getPropertyPath()).thenReturn(path);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(javax.validation.constraints.NotBlank.class);
    doReturn(javax.validation.constraints.NotBlank.class).when(annotation).annotationType();
    when(descriptor.getAnnotation()).thenReturn(annotation);
    doReturn(descriptor).when(violation).getConstraintDescriptor();
    when(violation.getMessage()).thenReturn("must not be blank");

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    WebRequest request = mock(WebRequest.class);

    ErrorResource result = handler.handleConstraintViolation(cve, request);

    assertNotNull(result);
    assertFalse(result.getFieldErrors().isEmpty());
    assertEquals("email", result.getFieldErrors().get(0).getField());
  }

  @Test
  void should_handle_single_segment_path() {
    ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) Object.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("email");
    when(violation.getPropertyPath()).thenReturn(path);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation2 = mock(javax.validation.constraints.NotBlank.class);
    doReturn(javax.validation.constraints.NotBlank.class).when(annotation2).annotationType();
    when(descriptor.getAnnotation()).thenReturn(annotation2);
    doReturn(descriptor).when(violation).getConstraintDescriptor();
    when(violation.getMessage()).thenReturn("must not be blank");

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    WebRequest request = mock(WebRequest.class);
    ErrorResource result = handler.handleConstraintViolation(cve, request);

    assertNotNull(result);
    assertEquals("email", result.getFieldErrors().get(0).getField());
  }
}

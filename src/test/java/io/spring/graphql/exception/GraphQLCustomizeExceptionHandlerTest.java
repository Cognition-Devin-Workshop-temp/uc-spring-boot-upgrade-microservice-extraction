package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import io.spring.api.exception.InvalidAuthenticationException;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
  }

  @SuppressWarnings("unchecked")
  private ConstraintViolation<?> createMockViolation(String propertyPath, String message) {
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn(propertyPath);
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
    when(violation.getMessage()).thenReturn(message);
    return violation;
  }

  @Test
  void should_handle_authentication_exception() {
    InvalidAuthenticationException ex = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(ex);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("createUser.param.email", "can't be empty"));

    ConstraintViolationException cve =
        new ConstraintViolationException("validation failed", violations);
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(cve);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_delegate_to_default_handler_for_other_exceptions() {
    RuntimeException ex = new RuntimeException("something went wrong");
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(ex);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @Test
  void should_get_errors_as_data() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("createUser.param.email", "can't be empty"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    io.spring.graphql.types.Error result = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(result);
    assertEquals("BAD_REQUEST", result.getMessage());
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_handle_simple_property_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("email", "invalid"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    io.spring.graphql.types.Error result = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }
}

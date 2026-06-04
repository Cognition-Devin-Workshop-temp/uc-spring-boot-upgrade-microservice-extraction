package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;

  @Mock private DataFetchingEnvironment dataFetchingEnvironment;
  @Mock private ExecutionStepInfo executionStepInfo;

  @BeforeEach
  void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
  }

  private DataFetcherExceptionHandlerParameters buildParams(Throwable exception) {
    when(dataFetchingEnvironment.getExecutionStepInfo()).thenReturn(executionStepInfo);
    when(executionStepInfo.getPath()).thenReturn(ResultPath.rootPath());
    return DataFetcherExceptionHandlerParameters.newExceptionParameters()
        .dataFetchingEnvironment(dataFetchingEnvironment)
        .exception(exception)
        .build();
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException ex = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters params = buildParams(ex);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @SuppressWarnings("unchecked")
  private ConstraintViolation<?> createMockViolation(String pathStr, String message) {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    Path path = mock(Path.class);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation =
        new javax.validation.constraints.NotBlank() {
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
            return javax.validation.constraints.NotBlank.class;
          }
        };

    when(path.toString()).thenReturn(pathStr);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getRootBeanClass()).thenReturn(String.class);
    when(violation.getMessage()).thenReturn(message);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    return violation;
  }

  @Test
  void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("createUser.param.email", "must not be empty"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    DataFetcherExceptionHandlerParameters params = buildParams(cve);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_handle_generic_exception_with_default_handler() {
    RuntimeException ex = new RuntimeException("something went wrong");
    DataFetcherExceptionHandlerParameters params = buildParams(ex);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @Test
  void should_get_errors_as_data_from_constraint_violations() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("createUser.param.email", "already taken"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertFalse(error.getErrors().isEmpty());
    assertEquals("email", error.getErrors().get(0).getKey());
  }

  @Test
  void should_handle_single_segment_path_in_get_param() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("fieldName", "invalid"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("fieldName", error.getErrors().get(0).getKey());
  }

  @Test
  void should_aggregate_multiple_violations_for_same_field() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("createUser.param.email", "must not be empty"));
    violations.add(createMockViolation("createUser.param.email", "invalid format"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals(1, error.getErrors().size());
    assertEquals("email", error.getErrors().get(0).getKey());
    assertEquals(2, error.getErrors().get(0).getValue().size());
  }
}

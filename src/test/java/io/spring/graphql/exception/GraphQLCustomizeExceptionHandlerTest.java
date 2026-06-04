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
import org.junit.jupiter.api.Test;

class GraphQLCustomizeExceptionHandlerTest {

  private final GraphQLCustomizeExceptionHandler handler = new GraphQLCustomizeExceptionHandler();

  private DataFetcherExceptionHandlerParameters buildParams(Throwable exception) {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    ExecutionStepInfo stepInfo = mock(ExecutionStepInfo.class);
    when(stepInfo.getPath()).thenReturn(ResultPath.rootPath());
    when(dfe.getExecutionStepInfo()).thenReturn(stepInfo);
    return DataFetcherExceptionHandlerParameters.newExceptionParameters()
        .dataFetchingEnvironment(dfe)
        .exception(exception)
        .build();
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters params = buildParams(exception);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
    assertTrue(result.getErrors().get(0).getMessage().contains("invalid email or password"));
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_handle_constraint_violation_exception() {
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
    ConstraintViolationException cve = new ConstraintViolationException("error", violations);

    DataFetcherExceptionHandlerParameters params = buildParams(cve);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_delegate_generic_exception() {
    RuntimeException exception = new RuntimeException("generic error");
    DataFetcherExceptionHandlerParameters params = buildParams(exception);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_get_errors_as_data() {
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

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertFalse(error.getErrors().isEmpty());
    assertEquals("email", error.getErrors().get(0).getKey());
  }
}

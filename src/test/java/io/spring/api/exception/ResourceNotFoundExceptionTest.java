package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ResourceNotFoundExceptionTest {

  @Test
  void should_construct_exception() {
    ResourceNotFoundException exception = new ResourceNotFoundException();

    assertNotNull(exception);
    assertInstanceOf(RuntimeException.class, exception);
  }
}

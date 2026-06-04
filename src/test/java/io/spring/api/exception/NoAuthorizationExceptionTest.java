package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NoAuthorizationExceptionTest {

  @Test
  void should_construct_exception() {
    NoAuthorizationException exception = new NoAuthorizationException();

    assertNotNull(exception);
    assertInstanceOf(RuntimeException.class, exception);
  }
}

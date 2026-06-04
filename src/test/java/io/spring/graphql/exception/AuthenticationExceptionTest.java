package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AuthenticationExceptionTest {

  @Test
  void should_construct_authentication_exception() {
    AuthenticationException exception = new AuthenticationException();

    assertNotNull(exception);
    assertInstanceOf(RuntimeException.class, exception);
  }
}

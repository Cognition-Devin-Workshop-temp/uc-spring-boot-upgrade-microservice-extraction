package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

class InvalidRequestExceptionTest {

  @Test
  void should_construct_and_return_errors() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult("target", "target");
    errors.addError(new FieldError("target", "title", "can't be empty"));

    InvalidRequestException exception = new InvalidRequestException(errors);

    assertNotNull(exception.getErrors());
    assertEquals(1, exception.getErrors().getFieldErrorCount());
    assertEquals("title", exception.getErrors().getFieldErrors().get(0).getField());
  }
}

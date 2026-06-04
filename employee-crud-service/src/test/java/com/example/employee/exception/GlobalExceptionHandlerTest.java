package com.example.employee.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleEmployeeNotFoundException_shouldReturnNotFoundResponse() {
        EmployeeNotFoundException ex = new EmployeeNotFoundException("Employee not found with id: 1");

        ResponseEntity<Map<String, Object>> response = handler.handleEmployeeNotFoundException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(404);
        assertThat(response.getBody().get("error")).isEqualTo("Not Found");
        assertThat(response.getBody().get("message")).isEqualTo("Employee not found with id: 1");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }
}

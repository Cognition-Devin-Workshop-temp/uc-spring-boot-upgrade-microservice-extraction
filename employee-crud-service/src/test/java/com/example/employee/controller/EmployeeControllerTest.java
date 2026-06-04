package com.example.employee.controller;

import com.example.employee.exception.EmployeeNotFoundException;
import com.example.employee.model.Employee;
import com.example.employee.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee("John Doe", 75000.0, 30);
        employee.setEmployeeId(1L);
    }

    @Test
    void createEmployee_shouldReturnCreatedEmployee() throws Exception {
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(employee);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeId", is(1)))
                .andExpect(jsonPath("$.employeeName", is("John Doe")))
                .andExpect(jsonPath("$.employeeSalary", is(75000.0)))
                .andExpect(jsonPath("$.employeeAge", is(30)));
    }

    @Test
    void createEmployee_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
        Employee invalid = new Employee("", 75000.0, 30);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.employeeName").exists());
    }

    @Test
    void createEmployee_shouldReturnBadRequest_whenSalaryIsNegative() throws Exception {
        Employee invalid = new Employee("John Doe", -1000.0, 30);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.employeeSalary").exists());
    }

    @Test
    void createEmployee_shouldReturnBadRequest_whenAgeIsBelow18() throws Exception {
        Employee invalid = new Employee("John Doe", 75000.0, 15);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.employeeAge").exists());
    }

    @Test
    void getEmployeeById_shouldReturnEmployee() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(employee);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId", is(1)))
                .andExpect(jsonPath("$.employeeName", is("John Doe")))
                .andExpect(jsonPath("$.employeeSalary", is(75000.0)))
                .andExpect(jsonPath("$.employeeAge", is(30)));
    }

    @Test
    void getEmployeeById_shouldReturnNotFound_whenNotExists() throws Exception {
        when(employeeService.getEmployeeById(99L))
                .thenThrow(new EmployeeNotFoundException("Employee not found with id: 99"));

        mockMvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Employee not found with id: 99")));
    }

    @Test
    void getAllEmployees_shouldReturnEmployeeList() throws Exception {
        Employee employee2 = new Employee("Jane Smith", 80000.0, 28);
        employee2.setEmployeeId(2L);
        List<Employee> employees = Arrays.asList(employee, employee2);
        when(employeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].employeeName", is("John Doe")))
                .andExpect(jsonPath("$[1].employeeName", is("Jane Smith")));
    }

    @Test
    void updateEmployee_shouldReturnUpdatedEmployee() throws Exception {
        Employee updated = new Employee("John Updated", 85000.0, 31);
        updated.setEmployeeId(1L);
        when(employeeService.updateEmployee(eq(1L), any(Employee.class))).thenReturn(updated);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeName", is("John Updated")))
                .andExpect(jsonPath("$.employeeSalary", is(85000.0)))
                .andExpect(jsonPath("$.employeeAge", is(31)));
    }

    @Test
    void updateEmployee_shouldReturnNotFound_whenNotExists() throws Exception {
        Employee updated = new Employee("John Updated", 85000.0, 31);
        when(employeeService.updateEmployee(eq(99L), any(Employee.class)))
                .thenThrow(new EmployeeNotFoundException("Employee not found with id: 99"));

        mockMvc.perform(put("/api/employees/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Employee not found with id: 99")));
    }

    @Test
    void updateEmployee_shouldReturnBadRequest_whenInvalidData() throws Exception {
        Employee invalid = new Employee("", -100.0, 10);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteEmployee_shouldReturnNoContent() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee(1L);
    }

    @Test
    void deleteEmployee_shouldReturnNotFound_whenNotExists() throws Exception {
        doThrow(new EmployeeNotFoundException("Employee not found with id: 99"))
                .when(employeeService).deleteEmployee(99L);

        mockMvc.perform(delete("/api/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Employee not found with id: 99")));
    }
}

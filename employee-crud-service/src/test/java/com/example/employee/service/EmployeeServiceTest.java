package com.example.employee.service;

import com.example.employee.exception.EmployeeNotFoundException;
import com.example.employee.model.Employee;
import com.example.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee("John Doe", 75000.0, 30);
        employee.setEmployeeId(1L);
    }

    @Test
    void createEmployee_shouldSaveAndReturnEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee result = employeeService.createEmployee(employee);

        assertThat(result).isNotNull();
        assertThat(result.getEmployeeName()).isEqualTo("John Doe");
        assertThat(result.getEmployeeSalary()).isEqualTo(75000.0);
        assertThat(result.getEmployeeAge()).isEqualTo(30);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void getEmployeeById_shouldReturnEmployee_whenExists() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Employee result = employeeService.getEmployeeById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getEmployeeId()).isEqualTo(1L);
        assertThat(result.getEmployeeName()).isEqualTo("John Doe");
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void getEmployeeById_shouldThrowException_whenNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee not found with id: 99");
    }

    @Test
    void getAllEmployees_shouldReturnAllEmployees() {
        Employee employee2 = new Employee("Jane Smith", 80000.0, 28);
        employee2.setEmployeeId(2L);
        when(employeeRepository.findAll()).thenReturn(Arrays.asList(employee, employee2));

        List<Employee> result = employeeService.getAllEmployees();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEmployeeName()).isEqualTo("John Doe");
        assertThat(result.get(1).getEmployeeName()).isEqualTo("Jane Smith");
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void updateEmployee_shouldUpdateAndReturnEmployee() {
        Employee updatedDetails = new Employee("John Updated", 85000.0, 31);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee result = employeeService.updateEmployee(1L, updatedDetails);

        assertThat(result.getEmployeeName()).isEqualTo("John Updated");
        assertThat(result.getEmployeeSalary()).isEqualTo(85000.0);
        assertThat(result.getEmployeeAge()).isEqualTo(31);
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void updateEmployee_shouldThrowException_whenNotFound() {
        Employee updatedDetails = new Employee("John Updated", 85000.0, 31);
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployee(99L, updatedDetails))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee not found with id: 99");
    }

    @Test
    void deleteEmployee_shouldDeleteEmployee_whenExists() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(1L);

        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).delete(employee);
    }

    @Test
    void deleteEmployee_shouldThrowException_whenNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.deleteEmployee(99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee not found with id: 99");
    }
}

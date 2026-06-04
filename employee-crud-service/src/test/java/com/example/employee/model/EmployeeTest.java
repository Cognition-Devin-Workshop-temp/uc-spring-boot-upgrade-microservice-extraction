package com.example.employee.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeTest {

    @Test
    void testNoArgsConstructor() {
        Employee employee = new Employee();
        assertThat(employee.getEmployeeId()).isNull();
        assertThat(employee.getEmployeeName()).isNull();
        assertThat(employee.getEmployeeSalary()).isNull();
        assertThat(employee.getEmployeeAge()).isNull();
        assertThat(employee.getEmployeeStatus()).isNull();
    }

    @Test
    void testParameterizedConstructor() {
        Employee employee = new Employee("John Doe", 75000.0, 30, EmployeeStatus.NEW);
        assertThat(employee.getEmployeeName()).isEqualTo("John Doe");
        assertThat(employee.getEmployeeSalary()).isEqualTo(75000.0);
        assertThat(employee.getEmployeeAge()).isEqualTo(30);
        assertThat(employee.getEmployeeStatus()).isEqualTo(EmployeeStatus.NEW);
    }

    @Test
    void testSettersAndGetters() {
        Employee employee = new Employee();
        employee.setEmployeeId(1L);
        employee.setEmployeeName("Jane Smith");
        employee.setEmployeeSalary(80000.0);
        employee.setEmployeeAge(28);
        employee.setEmployeeStatus(EmployeeStatus.EXISTING);

        assertThat(employee.getEmployeeId()).isEqualTo(1L);
        assertThat(employee.getEmployeeName()).isEqualTo("Jane Smith");
        assertThat(employee.getEmployeeSalary()).isEqualTo(80000.0);
        assertThat(employee.getEmployeeAge()).isEqualTo(28);
        assertThat(employee.getEmployeeStatus()).isEqualTo(EmployeeStatus.EXISTING);
    }
}

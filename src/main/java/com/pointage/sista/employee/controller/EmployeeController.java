package com.pointage.sista.employee.controller;

import com.pointage.sista.employee.entity.Employee;
import com.pointage.sista.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;
    /**
     * Create a new employee
     */
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee){
        Employee savedEmployee = employeeService.create(employee);
        return new ResponseEntity<>(savedEmployee, HttpStatus.CREATED);
    }

    /**
     * Get all employees
     */
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.findAll());
    }

    /**
     * Get employee by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return employeeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Disable (soft delete) an employee
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateEmployee(@PathVariable Long id) {
        employeeService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}

package com.pointage.sista.employee.service;

import com.pointage.sista.employee.entity.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {
    Employee create(Employee employee);
    Optional<Employee> findById(Long id);
    List<Employee> findAll();
    void deactivate(Long id);
}

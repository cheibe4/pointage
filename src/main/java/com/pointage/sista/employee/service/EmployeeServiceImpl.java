package com.pointage.sista.employee.service;

import com.pointage.sista.employee.entity.Employee;
import com.pointage.sista.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService{
    private final EmployeeRepository employeeRepository;


    @Override
    public Employee create(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Override
    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    @Override
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Override
    public void deactivate(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("Employee Not Found"));

        employee.setActive(false);
    }
}

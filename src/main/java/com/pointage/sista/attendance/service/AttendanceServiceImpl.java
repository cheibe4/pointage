package com.pointage.sista.attendance.service;
import com.pointage.sista.attendance.entity.Attendance;
import com.pointage.sista.attendance.repository.AttendanceRepository;
import com.pointage.sista.employee.entity.Employee;
import com.pointage.sista.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceServiceImpl implements AttendanceService{

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public Attendance checkIn(Long employeeId) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        if (!employee.isActive()) {
            throw new IllegalStateException("Inactive employee cannot check in");
        }

        LocalDate today = LocalDate.now();

        attendanceRepository.findByEmployeeAndWorkDate(employee, today)
                .ifPresent(a -> {
                    throw new IllegalStateException("Employee already checked in today");
                });

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .workDate(today)
                .checkInTime(LocalDateTime.now())
                .build();

        return attendanceRepository.save(attendance);
    }

    @Override
    public Attendance checkOut(Long employeeId) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository
                .findByEmployeeAndWorkDate(employee, today)
                .orElseThrow(() -> new IllegalStateException("Employee has not checked in today"));

        if (attendance.getCheckOutTime() != null) {
            throw new IllegalStateException("Employee already checked out");
        }

        attendance.setCheckOutTime(LocalDateTime.now());

        return attendanceRepository.save(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> getAttendanceHistory(Long employeeId) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        return attendanceRepository.findAllByEmployeeOrderByWorkDateDesc(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> getAttendanceBetweenDates(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate
    ) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        return attendanceRepository.findAllByEmployeeAndWorkDateBetween(
                employee,
                startDate,
                endDate
        );
    }


}
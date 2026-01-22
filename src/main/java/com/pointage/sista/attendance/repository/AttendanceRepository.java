package com.pointage.sista.attendance.repository;

import com.pointage.sista.attendance.entity.Attendance;
import com.pointage.sista.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {


    /**
     * Find attendance for a given employee and day
     * Used to prevent double check-in
     */
    Optional<Attendance> findByEmployeeAndWorkDate(Employee employee, LocalDate workDate);

    /**
     * Attendance history for an employee
     */
    List<Attendance> findAllByEmployeeOrderByWorkDateDesc(Employee employee);

    /**
     * Attendance between two dates (week / month / year)
     */
    List<Attendance> findAllByEmployeeAndWorkDateBetween(
            Employee employee,
            LocalDate startDate,
            LocalDate endDate
    );

    /**
     * Employees attendance per day
     */
    List<Attendance> findAllByWorkDate(LocalDate workDate);

    /**
     * Find employees currently present (checked-in but not checked-out)
     */
    List<Attendance> findAllByCheckOutTimeIsNull();
}

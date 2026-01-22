package com.pointage.sista.attendance.service;

import com.pointage.sista.attendance.entity.Attendance;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    Attendance checkIn(Long employeeId);

    Attendance checkOut(Long employeeId);

    List<Attendance> getAttendanceHistory(Long employeeId);

    List<Attendance> getAttendanceBetweenDates(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Attendance> getDailyAttendance(LocalDate date);
}

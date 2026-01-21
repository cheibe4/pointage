package com.pointage.sista.attendance.controller;

import com.pointage.sista.attendance.entity.Attendance;
import com.pointage.sista.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    /**
     * Check-in (arrival)
     */
    @PostMapping("/check-in/{employeeId}")
    public ResponseEntity<Attendance> checkIn(@PathVariable Long employeeId) {
        Attendance attendance = attendanceService.checkIn(employeeId);
        return ResponseEntity.ok(attendance);
    }

    /**
     * Check-out (departure)
     */
    @PostMapping("/check-out/{employeeId}")
    public ResponseEntity<Attendance> checkOut(@PathVariable Long employeeId) {
        Attendance attendance = attendanceService.checkOut(employeeId);
        return ResponseEntity.ok(attendance);
    }

    /**
     * Attendance history for an employee
     */
    @GetMapping("/history/{employeeId}")
    public ResponseEntity<List<Attendance>> getHistory(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.getAttendanceHistory(employeeId));
    }

    /**
     * Attendance between dates (week/month/year)
     * Example:
     * /api/attendance/range/5?startDate=2026-01-01&endDate=2026-01-31
     */
    @GetMapping("/range/{employeeId}")
    public ResponseEntity<List<Attendance>> getAttendanceBetweenDates(
            @PathVariable Long employeeId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(
                attendanceService.getAttendanceBetweenDates(employeeId, startDate, endDate)
        );
    }
}

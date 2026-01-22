package com.pointage.sista.attendance.controller;

import com.pointage.sista.attendance.entity.Attendance;
import com.pointage.sista.attendance.service.AttendanceService;
import com.pointage.sista.attendance.service.ExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final ExcelService excelService;

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

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportAttendanceToExcel(
            @RequestParam Long employeeId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ){
        List<Attendance> attendances
                = attendanceService.getAttendanceBetweenDates(employeeId, startDate, endDate);
        ByteArrayInputStream stream = excelService.exportAttendance(attendances);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition","attachment; filename=attendance.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ))
                .body(new InputStreamResource(stream));
    }

    @GetMapping("/export/daily")
    public ResponseEntity<InputStreamResource> exportDailyAttendance(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        ByteArrayInputStream stream = excelService.exportDailyAttendance(date);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition",
                "attachment; filename=attendance_" + date + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(stream));
    }

}

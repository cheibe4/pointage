package com.pointage.sista.attendance.service;

import com.pointage.sista.attendance.entity.Attendance;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

public interface ExcelService {
    ByteArrayInputStream exportAttendance(List<Attendance> attendances);

    ByteArrayInputStream exportDailyAttendance(LocalDate date);

    ByteArrayInputStream exportWeeklyAttendance(LocalDate anyDateInWeek);

}

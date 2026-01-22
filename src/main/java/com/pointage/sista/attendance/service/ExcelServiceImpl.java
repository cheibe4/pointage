package com.pointage.sista.attendance.service;

import com.pointage.sista.attendance.entity.Attendance;
import com.pointage.sista.attendance.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService{
    private final AttendanceRepository attendanceRepository;
    @Override
    public ByteArrayInputStream exportAttendance(List<Attendance> attendances){
        try(Workbook workbook = new XSSFWorkbook()){
            Sheet sheet = workbook.createSheet("Attendance");

            //Header
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Employee ID", "Employee Name", "Date", "Check In", "Check Out"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            //Data
            int rowIndex = 1;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            for(Attendance attendance : attendances) {
                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(attendance.getEmployee().getId());
                row.createCell(1).setCellValue(attendance.getEmployee().getName());
                row.createCell(2).setCellValue(attendance.getWorkDate().format(dateFormatter));

                if(attendance.getCheckInTime() != null) {
                    row.createCell(3).setCellValue(attendance.getCheckInTime().format(timeFormatter));
                }

                if(attendance.getCheckOutTime() != null){
                    row.createCell(4).setCellValue(attendance.getCheckOutTime().format(timeFormatter));
                }

            }

            //Auto-size columns
            for(int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            return new ByteArrayInputStream(outputStream.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Failed to export data to Excel file: " + e.getMessage());
        }
    }

    @Override
    public ByteArrayInputStream exportDailyAttendance(LocalDate date) {
        List<Attendance> attendances
                = attendanceRepository.findAllByWorkDate(date);

        try(Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()){
            Sheet sheet = workbook.createSheet("Daily Attendance");

            // ================= HEADER STYLE =================
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // ================= HEADER ROW =================
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Employee ID",
                    "Full Name",
                    "Check-in Time",
                    "Check-out Time"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ================= DATA ROWS =================
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            int rowIndex = 1;
            for (Attendance attendance : attendances) {
                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(attendance.getEmployee().getId());
                row.createCell(1).setCellValue(attendance.getEmployee().getName());
                row.createCell(4).setCellValue(
                        attendance.getCheckInTime() != null
                                ? attendance.getCheckInTime().format(timeFormatter)
                                : ""
                );
                row.createCell(5).setCellValue(
                        attendance.getCheckOutTime() != null
                                ? attendance.getCheckOutTime().format(timeFormatter)
                                : ""
                );
            }
            // ================= AUTO-SIZE =================
            for(int i = 0; i < headers.length; i++){
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        }catch (Exception e){
            throw new RuntimeException("Failed to export data to Excel file", e);
        }
    }

}

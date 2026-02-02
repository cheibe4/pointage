package com.pointage.sista.attendance.service;

import com.pointage.sista.attendance.entity.Attendance;
import com.pointage.sista.employee.entity.Employee;
import com.pointage.sista.employee.repository.EmployeeRepository;
import com.pointage.sista.attendance.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService{
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
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

            // ================= LOGO (CENTERED) =================
            InputStream logoStream =
                    getClass().getResourceAsStream("/logo/sista.png");

            byte[] logoBytes = IOUtils.toByteArray(logoStream);
            int pictureIdx = workbook.addPicture(logoBytes, Workbook.PICTURE_TYPE_PNG);
            logoStream.close();

            CreationHelper helper = workbook.getCreationHelper();
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            ClientAnchor anchor = helper.createClientAnchor();

            // Center logo across columns 1 to 3
            anchor.setCol1(1);
            anchor.setRow1(0);
            anchor.setCol2(3);
            anchor.setRow2(3);

            drawing.createPicture(anchor, pictureIdx);

            // ================= TITLE STYLE =================
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            // ================= TITLE ROW =================
            Row titleRow = sheet.createRow(3);
            DateTimeFormatter titleFormatter =
                    DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy");

            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(
                    "Daily Attendance – " + date.format(titleFormatter)
            );
            titleCell.setCellStyle(titleStyle);

            sheet.addMergedRegion(new CellRangeAddress(
                    3, 3, 0, 3
            ));


            // ================= HEADER STYLE =================
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            // ================= HEADER ROW =================
            Row headerRow = sheet.createRow(5);
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
            int rowIndex = 6;
            for (Attendance attendance : attendances) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(attendance.getEmployee().getId());
                row.createCell(1).setCellValue(attendance.getEmployee().getName());
                row.createCell(2).setCellValue(
                        attendance.getCheckInTime() != null
                                ? attendance.getCheckInTime().format(timeFormatter)
                                : ""
                );
                row.createCell(3).setCellValue(
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

    @Override
    public ByteArrayInputStream exportWeeklyAttendance(LocalDate date) {

        // ================= VALIDATION =================
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot export future weeks");
        }

        // Monday → Saturday (Sunday excluded)
        LocalDate startOfWeek = date.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(5);

        List<Employee> employees = employeeRepository.findAll();

        List<Attendance> attendances =
                attendanceRepository.findAllByWorkDateBetween(startOfWeek, endOfWeek);

        // Map: employeeId → (date → attendance)
        Map<Long, Map<LocalDate, Attendance>> attendanceMap = new HashMap<>();

        for (Attendance a : attendances) {
            attendanceMap
                    .computeIfAbsent(a.getEmployee().getId(), k -> new HashMap<>())
                    .put(a.getWorkDate(), a);
        }

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Weekly Attendance");

            // ================= STYLES =================
            CellStyle centerStyle = workbook.createCellStyle();
            centerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle leftStyle = workbook.createCellStyle();
            leftStyle.setAlignment(HorizontalAlignment.LEFT);

            // ================= TITLE =================
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);

            int weekNumber = startOfWeek.get(WeekFields.ISO.weekOfWeekBasedYear());

            titleCell.setCellValue(
                    "Semaine " + weekNumber + " : " +
                            startOfWeek + " → " + endOfWeek
            );

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            titleCell.setCellStyle(titleStyle);

            sheet.addMergedRegion(new CellRangeAddress(
                    0, 0, 0, 13
            ));

            // ================= HEADERS =================
            Row dayHeaderRow = sheet.createRow(2);
            Row subHeaderRow = sheet.createRow(3);

            int col = 0;

            // Employee columns
            Cell empIdHeader = dayHeaderRow.createCell(col);
            empIdHeader.setCellValue("ID");
            empIdHeader.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
            col++;

            Cell empNameHeader = dayHeaderRow.createCell(col);
            empNameHeader.setCellValue("Nom");
            empNameHeader.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
            col++;

            // ================= DAYS (FRENCH) =================
            DateTimeFormatter frenchDayFormatter =
                    DateTimeFormatter.ofPattern("EEEE", Locale.FRENCH);

            for (int i = 0; i < 6; i++) {
                LocalDate day = startOfWeek.plusDays(i);

                Cell dayCell = dayHeaderRow.createCell(col);
                dayCell.setCellValue(frenchDayFormatter.format(day));
                dayCell.setCellStyle(headerStyle);

                sheet.addMergedRegion(
                        new CellRangeAddress(2, 2, col, col + 1)
                );

                Cell inCell = subHeaderRow.createCell(col);
                inCell.setCellValue("Entrée");
                inCell.setCellStyle(headerStyle);

                Cell outCell = subHeaderRow.createCell(col + 1);
                outCell.setCellValue("Sortie");
                outCell.setCellStyle(headerStyle);

                col += 2;
            }

            // ================= DATA =================
            DateTimeFormatter timeFormatter =
                    DateTimeFormatter.ofPattern("HH:mm");

            int rowIndex = 4;

            for (Employee employee : employees) {
                Row row = sheet.createRow(rowIndex++);
                int c = 0;

                Cell idCell = row.createCell(c++);
                idCell.setCellValue(employee.getId());
                idCell.setCellStyle(leftStyle);

                Cell nameCell = row.createCell(c++);
                nameCell.setCellValue(employee.getName());
                nameCell.setCellStyle(leftStyle);

                Map<LocalDate, Attendance> empAttendances =
                        attendanceMap.getOrDefault(employee.getId(), Map.of());

                for (int i = 0; i < 6; i++) {
                    LocalDate day = startOfWeek.plusDays(i);
                    Attendance a = empAttendances.get(day);

                    row.createCell(c++).setCellValue(
                            a != null && a.getCheckInTime() != null
                                    ? a.getCheckInTime().format(timeFormatter)
                                    : ""
                    );

                    row.createCell(c++).setCellValue(
                            a != null && a.getCheckOutTime() != null
                                    ? a.getCheckOutTime().format(timeFormatter)
                                    : ""
                    );
                }
            }

            // ================= COLUMN WIDTHS =================
            sheet.setColumnWidth(0, 3000);
            sheet.setColumnWidth(1, 6000);

            for (int i = 2; i <= 13; i++) {
                sheet.setColumnWidth(i, 3500);
            }

            sheet.createFreezePane(0, 4);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Weekly export failed", e);
        }
    }


}

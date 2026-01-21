package com.pointage.sista.attendance.entity;

import com.pointage.sista.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Entity
@Table(
        name = "attendance",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_employee_day",
                        columnNames = {"employee_id", "attendance_date"}
                )
        },
        indexes = {
                @Index(name = "idx_attendance_employee", columnList = "employee_id"),
                @Index(name = "idx_attendance_attendance_date", columnList = "attendance_date")
        }
)
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Employee linked to this attendance record
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "employee_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_attendance_employee")
    )
    private Employee employee;

    /**
     * Logical working day (important for reporting)
     */
    @Column(name = "attendance_date", nullable = false)
    private LocalDate workDate;

    /**
     * Arrival time (check-in)
     */
    @Column(name = "check_in_time", nullable = false)
    private LocalTime checkInTime;

    /**
     * Departure time (check-out)
     * Nullable until the employee leaves
     */
    @Column(name = "check_out_time")
    private LocalTime checkOutTime;
}

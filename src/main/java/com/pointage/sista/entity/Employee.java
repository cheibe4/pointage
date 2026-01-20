package com.pointage.sista.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(
        name = "employees",
        indexes = {
                @Index(name = "idx_employee_department", columnList = "department"),
                @Index(name = "idx_employee_role", columnList = "role")
        }
)
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 100)
    private String role;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(nullable = false)
    private boolean active = true;

}


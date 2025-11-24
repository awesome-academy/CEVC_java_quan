package com.example.member_management_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class ProjectDTO {
    private Long id;

    @NotBlank(message = "{validation.project.name.notblank}")
    @Size(max = 255)
    private String name;

    @Size(max = 50)
    private String abbreviation;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @NotNull(message = "{validation.project.team.notnull}")
    private Long teamId;

    private Long leaderId; // PM (Optional initially)

    private int status = 1; // Default Active
}

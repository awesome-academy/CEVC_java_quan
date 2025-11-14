package com.example.member_management_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PositionDTO {

    private Long id;

    @NotBlank(message = "{validation.position.name.notblank}")
    @Size(min = 2, max = 255, message = "{validation.position.name.size}")
    private String name;

    @Size(max = 50, message = "{validation.position.abbreviation.size}")
    private String abbreviation;
}

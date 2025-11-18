package com.example.member_management_system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SkillDTO {

    private Long id;

    @NotBlank(message = "{validation.skill.name.notblank}")
    @Size(min = 2, max = 255, message = "{validation.skill.name.size}")
    private String name;

    @Size(max = 50, message = "{validation.skill.level.size}")
    private String level;

    @Min(value = 0, message = "{validation.skill.usedYears.min}")
    private int usedYears = 0;

    public SkillDTO(String name, String level, String usedYears) {
        this.name = name;
        this.level = level;
        try {
            this.usedYears = (usedYears == null || usedYears.isBlank()) ? 0 : Integer.parseInt(usedYears);
        } catch (NumberFormatException e) {
            this.usedYears = 0;
        }
    }
}

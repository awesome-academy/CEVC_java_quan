package com.example.member_management_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class MemberDTO {

    private Long id;

    @NotBlank(message = "{validation.member.fullname.notblank}")
    @Size(max = 255)
    private String fullName;

    @NotBlank(message = "{validation.member.email.notblank}")
    @Email(message = "{validation.member.email.invalid}")
    private String email;

    @NotBlank(message = "{validation.member.password.notblank}", groups = OnCreate.class)
    private String password;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    private boolean active = true;

    @NotNull(message = "{validation.member.position.notnull}")
    private Long positionId;

    // List of Role IDs (Checkboxes)
    private List<Long> roleIds = new ArrayList<>();

    // List of Skill IDs (Checkboxes/Multi-select)
    private List<Long> skillIds = new ArrayList<>();

    public interface OnCreate {
    }

    public interface OnUpdate {
    }
}

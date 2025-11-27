package com.example.member_management_system.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update user profile")
public class UpdateUserProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name must not exceed 255 characters")
    @Schema(description = "Full name", example = "John Doe")
    private String fullName;

    @Schema(description = "Birthday", example = "1990-01-15")
    private LocalDate birthday;

    @NotNull(message = "Position is required")
    @Schema(description = "Position ID", example = "1")
    private Long positionId;

    @Schema(description = "List of skill IDs", example = "[1, 2, 3]")
    private List<Long> skillIds;
}


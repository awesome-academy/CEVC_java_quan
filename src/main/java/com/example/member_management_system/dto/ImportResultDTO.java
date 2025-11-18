package com.example.member_management_system.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO to hold the result of a CSV import operation.
 * Contains the count of successfully imported items and a list of errors.
 */
@Data
@NoArgsConstructor
public class ImportResultDTO {

    private List<String> errors = new ArrayList<>();
    private int importedCount = 0;

    public boolean hasErrors() {
        return this.errors != null && !this.errors.isEmpty();
    }
}

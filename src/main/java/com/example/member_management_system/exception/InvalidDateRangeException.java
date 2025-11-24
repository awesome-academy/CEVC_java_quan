package com.example.member_management_system.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when date validation fails (e.g., end date before start date).
 */
@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDateRangeException extends BusinessException {

    private final String startDateField;
    private final String endDateField;

    public InvalidDateRangeException(String message) {
        super(message);
        this.startDateField = null;
        this.endDateField = null;
    }

    public InvalidDateRangeException(String message, String startDateField, String endDateField) {
        super(message);
        this.startDateField = startDateField;
        this.endDateField = endDateField;
    }

}


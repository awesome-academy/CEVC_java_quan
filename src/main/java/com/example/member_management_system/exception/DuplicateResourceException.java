package com.example.member_management_system.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to create or update a resource with a name/identifier
 * that already exists in the system.
 */
@Getter
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends BusinessException {

    private final String fieldName;
    private final Object fieldValue;

    public DuplicateResourceException(String message) {
        super(message);
        this.fieldName = null;
        this.fieldValue = null;
    }

    public DuplicateResourceException(String message, String fieldName, Object fieldValue) {
        super(message);
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

}


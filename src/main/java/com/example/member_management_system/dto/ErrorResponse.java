package com.example.member_management_system.dto;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class ErrorResponse {
    private int status;
    private String message;
    private String path;
    private Date timestamp;
    private Map<String, String> validationErrors;

    public ErrorResponse(int status, String message, String path) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.timestamp = new Date();
    }

    public ErrorResponse(int status, String message, String path, Map<String, String> validationErrors) {
        this(status, message, path);
        this.validationErrors = validationErrors;
    }
}

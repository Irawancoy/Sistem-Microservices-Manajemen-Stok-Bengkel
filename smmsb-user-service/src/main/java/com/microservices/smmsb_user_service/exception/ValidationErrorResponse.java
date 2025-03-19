package com.microservices.smmsb_user_service.exception;

import java.util.Map;

import com.microservices.smmsb_user_service.dto.response.MessageResponse;


public class ValidationErrorResponse extends MessageResponse {
    private Map<String, String> errors;

    public ValidationErrorResponse( String message, int statusCode, String status, Map<String, String> errors) {
        super(message,statusCode, status);
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}

package com.microservices.smmsb_inventory_service.exception;

import org.springframework.security.core.AuthenticationException;

public class AuthenticationFailedException extends AuthenticationException {
    public AuthenticationFailedException(String message) {
        super(message);
    }
}
package com.smartgig.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends SmartGigException {
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}


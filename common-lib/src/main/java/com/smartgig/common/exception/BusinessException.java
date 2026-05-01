package com.smartgig.common.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends SmartGigException {
    public BusinessException(String message) {
        super(message, "BUSINESS_ERROR", HttpStatus.BAD_REQUEST);
    }
}


package com.smartgig.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends SmartGigException {
    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
    }
}


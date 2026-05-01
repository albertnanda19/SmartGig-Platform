package com.smartgig.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SmartGigException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;

    public SmartGigException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}


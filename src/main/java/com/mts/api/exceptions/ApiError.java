package com.mts.api.exceptions;

import org.springframework.http.HttpStatus;

public class ApiError extends RuntimeException {

    public ApiError(String message) {
        super(message);
    }

    public ApiErrorCode reason() {
        return ApiErrorCode.GENERAL_EXCEPTION;
    }

    public HttpStatus httpStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}

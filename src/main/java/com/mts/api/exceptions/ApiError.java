package com.mts.api.exceptions;

public class ApiError extends RuntimeException {

    public ApiError(String message) {
        super(message);
    }

    public ApiErrorCode reason() {
        return ApiErrorCode.GENERAL_EXCEPTION;
    }
}

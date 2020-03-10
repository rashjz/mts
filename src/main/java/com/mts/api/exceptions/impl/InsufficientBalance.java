package com.mts.api.exceptions.impl;

import com.mts.api.exceptions.ApiError;
import com.mts.api.exceptions.ApiErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;

@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
public class InsufficientBalance extends ApiError {

    private static final String MESSAGE = "There isn't enough balance";

    private final BigDecimal amount;

    public InsufficientBalance(BigDecimal amount) {
        super(MESSAGE);
        this.amount = amount;
    }

    @Override
    public ApiErrorCode reason() {
        return ApiErrorCode.VALIDATION_ERROR;
    }


    @Override
    public String getMessage() {
        return super.getMessage() + ": " + amount;
    }

}
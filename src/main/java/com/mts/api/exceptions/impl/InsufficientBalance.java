package com.mts.api.exceptions.impl;

import com.mts.api.exceptions.ApiError;
import com.mts.api.exceptions.ApiErrorCode;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

public class InsufficientBalance extends ApiError {

    private static final String MESSAGE = "There isn't enough balance";

    private final BigDecimal amount;

    public InsufficientBalance(BigDecimal amount) {
        super(MESSAGE);
        this.amount = amount;
    }

    @Override
    public ApiErrorCode reason() {
        return ApiErrorCode.ACCOUNT_LIST_LIMIT_REACHED;
    }

    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + ": " + amount;
    }

}
package com.mts.api.exceptions.impl;

import com.mts.api.exceptions.ApiError;
import com.mts.api.exceptions.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class AccountDoesNotExist extends ApiError  {

    private static final String MESSAGE = "Account does not exist";

    private final Long accountId;

    public AccountDoesNotExist(Long accountId) {
        super(MESSAGE);
        this.accountId = accountId;
    }

    @Override
    public ApiErrorCode reason() {
        return ApiErrorCode.GENERAL_EXCEPTION;
    }

    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + ": " + accountId;
    }

}

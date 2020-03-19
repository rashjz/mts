package com.mts.api.exceptions.handling;

import com.mts.api.exceptions.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class ApiErrorHandler  extends BaseErrorHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ApiError.class)
    protected ResponseEntity<Object> handleGeneralApiError(ApiError ex) {
        return response(ex.httpStatus(), ex.getMessage(), ex.reason(), ex);
    }
}

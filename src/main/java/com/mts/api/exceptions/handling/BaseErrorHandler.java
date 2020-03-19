package com.mts.api.exceptions.handling;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mts.api.exceptions.ApiErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static com.mts.api.exceptions.ApiErrorCode.GENERAL_EXCEPTION;
import static com.mts.api.exceptions.ApiErrorCode.VALIDATION_ERROR;

public abstract class BaseErrorHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        return response(HttpStatus.BAD_REQUEST, ExceptionParser.parse(ex), VALIDATION_ERROR, ex);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        return response(HttpStatus.BAD_REQUEST, ExceptionParser.parse(ex), VALIDATION_ERROR, ex);
    }

    @ExceptionHandler(HttpStatusCodeException.class)
    protected ResponseEntity<Object> handleHttpStatusCode(HttpStatusCodeException ex) {
        return response(ex.getStatusCode(), StringUtils.defaultString(ex.getMessage(), ""), GENERAL_EXCEPTION, ex);
    }


    @ExceptionHandler({Exception.class, Throwable.class})
    protected ResponseEntity<Object> handleOther(Throwable ex) {
        String message = "Unexpected error occurred. Please contact an administrator.";

        return response(HttpStatus.INTERNAL_SERVER_ERROR, message, GENERAL_EXCEPTION, ex);
    }

    ResponseEntity<Object> response(HttpStatus status, String message, ApiErrorCode code, Throwable ex) {
        ObjectNode body = new ObjectMapper().createObjectNode();
        body.putObject("error")
                .put("status", status.value())
                .put("reason", code.toString())
                .put("message", message);

        logger.error(ex.toString());

        if (logger.isDebugEnabled()) {
            logger.debug("request exception:", ex);
        }

        return new ResponseEntity<>(body, status);
    }
}

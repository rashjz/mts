package com.mts.api.exceptions.handling;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static java.lang.String.format;

class ExceptionParser {

    private ExceptionParser() {
    }

    static String parse(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof UnrecognizedPropertyException) {
            return parse((UnrecognizedPropertyException) cause);
        } else if (cause instanceof JsonMappingException) {
            return parse((JsonMappingException) cause);
        }

        return "A portion of this request is not valid";
    }

    private static String parse(UnrecognizedPropertyException ex) {
        return format("Unrecognized field \"%s\". Known fields: %s", ex.getPropertyName(), ex.getKnownPropertyIds());
    }

    private static String parse(JsonMappingException ex) {
        String error = "";
        String positionInfo = "";
        List<JsonMappingException.Reference> exceptionPath = ex.getPath();

        if (!exceptionPath.isEmpty()) {
            String field = exceptionPath.get(exceptionPath.size() - 1).getFieldName();

            if (field == null) {
                field = exceptionPath.get(0).getFieldName();
            }

            if (field != null) {
                error = "The field named \"" + field + "\" may have an invalid value or may be of the wrong type.";
            }
        } else {
            if (ex.getMessage().contains("Can not deserialize instance of java.util.ArrayList out of START_OBJECT token")) {
                error = "Expected an array starting with '[' but got the start of an object '{'.";
            }
        }

        if (ex.getLocation() != null) {
            positionInfo = " near character position " + (ex.getLocation().getCharOffset() + 1);
        }

        return format("A portion of this JSON request is not valid%s: %s", positionInfo, error);
    }

    static String parse(MethodArgumentNotValidException ex) {
        String message = "";
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();

        if (!errors.isEmpty()) {
            ObjectError firstError = errors.get(0);

            if (firstError instanceof FieldError) {
                FieldError error = (FieldError) firstError;

                message = format("The field named \"%s\" had an invalid value of \"%s\". %s",
                        error.getField(), error.getRejectedValue(), error.getDefaultMessage());
            }
        }

        return StringUtils.defaultString(message, "A portion of this request is not valid.");
    }
}

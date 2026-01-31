package com.dfbs.app.infra.config;

import com.dfbs.app.application.quote.QuoteValidationException;
import com.dfbs.app.infra.dto.ErrorResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public static final String AUTH_DENIED = "AUTH_DENIED";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String NOT_FOUND = "NOT_FOUND";

    @ExceptionHandler(QuoteValidationException.class)
    public ResponseEntity<ErrorResult> handleQuoteValidation(QuoteValidationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResult.of(ex.getMessage(), VALIDATION_ERROR));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResult> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResult.of(ex.getMessage(), VALIDATION_ERROR));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResult> handleSecurity(SecurityException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResult.of(ex.getMessage(), AUTH_DENIED));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResult> handleIllegalState(IllegalStateException ex) {
        String msg = ex.getMessage();
        boolean notFound = msg != null && (msg.contains("not found") || msg.contains("不存在"));
        HttpStatus status = notFound ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        String code = notFound ? NOT_FOUND : VALIDATION_ERROR;
        return ResponseEntity.status(status).body(ErrorResult.of(msg, code));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResult> handleResponseStatus(ResponseStatusException ex) {
        int value = ex.getStatusCode().value();
        String code = value == 404 ? NOT_FOUND : value == 403 ? AUTH_DENIED : "ERROR";
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ErrorResult.of(ex.getReason(), code));
    }
}

package com.dfbs.app.infra.config;

import com.dfbs.app.application.dicttype.DictTypeNotFoundException;
import com.dfbs.app.application.quote.QuoteValidationException;
import com.dfbs.app.infra.dto.ErrorResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    @ExceptionHandler(DictTypeNotFoundException.class)
    public ResponseEntity<ErrorResult> handleDictTypeNotFound(DictTypeNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResult.of(ex.getMessage(), DictTypeNotFoundException.MACHINE_CODE));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResult> handleResponseStatus(ResponseStatusException ex) {
        int value = ex.getStatusCode().value();
        String code = value == 404 ? NOT_FOUND : value == 403 ? AUTH_DENIED : "ERROR";
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ErrorResult.of(ex.getReason(), code));
    }

    /**
     * Catch-all: log full stack trace so 500 errors are never silent, then return 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResult> handleUnhandled(Exception e) {
        log.error("Unhandled exception: ", e);
        String message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResult.of(message, "INTERNAL_SERVER_ERROR"));
    }
}

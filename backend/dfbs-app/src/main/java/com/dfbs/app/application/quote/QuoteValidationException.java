package com.dfbs.app.application.quote;

/**
 * Exception thrown when quote validation fails during confirmation.
 * Contains Chinese error messages for user-friendly display.
 */
public class QuoteValidationException extends IllegalStateException {
    
    public QuoteValidationException(String message) {
        super(message);
    }
    
    public QuoteValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.mosip.authsdk.exception;

public class AuthenticatorCryptoException extends RuntimeException {
    private final String errorCode;
    private final String errorMessage;

    // Constructor to initialize errorCode and errorMessage
    public AuthenticatorCryptoException(String errorCode, String errorMessage) {
        super(errorMessage);  // Calls the superclass (RuntimeException) constructor
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    // Getter for the errorCode
    public String getErrorCode() {
        return errorCode;
    }

    // Getter for the errorMessage
    public String getErrorMessage() {
        return errorMessage;
    }
}

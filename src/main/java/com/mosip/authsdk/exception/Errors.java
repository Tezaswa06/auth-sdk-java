package com.mosip.authsdk.exception;

public enum Errors {
    // Enum values with associated messages
    AUT_CRY_001("Error Parsing Encryption Certificate provided in config file. File Name: %s"),
    AUT_CRY_002("Error Reading P12 file provided in config file. File Name: %s"),
    AUT_CRY_003("Error Encrypting Auth Data."),
    AUT_CRY_004("Error Signing Auth Request Data."),
    AUT_CRY_005("Controller Method Not Found For Method %s. Supported Methods are %s"),

    AUT_BAS_001("Not Able to process auth request."),

    AUT_OTP_001("No channels found. Please Pass otp=True or phone=True");

    // Field to store the message
    private final String message;

    // Constructor to initialize the message for each enum constant
    Errors(String message) {
        this.message = message;
    }

    // Method to format the message with arguments, similar to Python's format()
    public String getMessage(Object... args) {
        return String.format(message, args);
    }
}

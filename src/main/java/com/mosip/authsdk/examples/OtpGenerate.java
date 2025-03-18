package com.mosip.authsdk.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mosip.authsdk.authenticator.MosipAuthenticator;
import com.mosip.authsdk.config.MosipAuthProperties;
import com.mosip.authsdk.model.MOSIPOtpRequest;
import com.mosip.authsdk.model.MOSIPOtpResponse;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OtpGenerate {
    public static void main(String[] args) {
        // Load Spring Context
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.scan("com.mosip.authsdk");
        context.refresh();

        try {
            // Load Configuration
            MosipAuthProperties config = context.getBean(MosipAuthProperties.class);
            MosipAuthenticator authenticator = context.getBean(MosipAuthenticator.class);

            // Create OTP Request
            List<String> otpChannels = Arrays.asList("email", "phone"); // Ensures both channels are selected
            MOSIPOtpRequest otpRequest = new MOSIPOtpRequest(
                    "mosip.identity.otp",     // IDA Auth Request ID
                    "1.0",                    // IDA Auth Version
                    "4370296312658178",        // Individual ID
                    "VID",                     // Individual ID Type
                    "1234567890",              // Transaction ID
                    "2025-03-10T12:30:00Z",    // Timestamp (ISO 8601)
                    otpChannels,               // OTP Channels (email, phone)
                    null                       // Metadata (optional)
            );

            // Generate OTP
            ResponseEntity<String> responseEntity = authenticator.generateOtp(otpRequest);

            // Deserialize the response body
            MOSIPOtpResponse response;
            try {
                String responseBody = responseEntity.getBody();
                ObjectMapper objectMapper = new ObjectMapper();
                response = objectMapper.readValue(responseBody, MOSIPOtpResponse.class);
            } catch (IOException e) {
                System.err.println("Failed to parse OTP response: " + e.getMessage());
                return;
            }

            // Handle Response Errors
            if (response.getErrors() != null && response.getErrors().length > 0) {
                for (Map<String, Object> error : response.getErrors()) {
                    System.out.println(error.get("errorCode") + " : " + error.get("errorMessage"));
                }
                System.exit(1);
            }

            // Print Response Status Code & Body
            System.out.println("Response Status Code: " + response.getResponse().get("status"));
            System.out.println("Body = " + response);

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensure Spring Context is closed
            context.close();
        }
    }
}
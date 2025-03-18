package com.mosip.authsdk.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mosip.authsdk.authenticator.MosipAuthenticator;
import com.mosip.authsdk.config.MosipAuthProperties;
import com.mosip.authsdk.model.DemographicsModel;
import com.mosip.authsdk.model.MOSIPAuthResponse;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;


public class DemoAuth {
    public static void main(String[] args) {
        // Load Spring Context
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.scan("com.mosip.authsdk");
        context.refresh();

        try {
            // Load Configuration
            MosipAuthProperties config = context.getBean(MosipAuthProperties.class);
            MosipAuthenticator authenticator = context.getBean(MosipAuthenticator.class);

            // Create Demographics Data
            DemographicsModel demographicsData = new DemographicsModel();
            demographicsData.setDob("1992/01/01");

            System.out.println("Demographics data: " + demographicsData);

            // Perform Authentication
            ResponseEntity<String> responseEntity;
            try {
                responseEntity = authenticator.authenticate(
                        "auth",  // Authentication type
                        "4370296312658178",  // Individual ID
                        "VID",  // Individual ID Type
                        demographicsData,  // Demographics Data
                        null,  // OTP Value (not provided)
                        null,  // Biometrics (not provided)
                        true,  // Consent
                        null   // Transaction ID (optional)
                );
            } catch (Exception e) {
                System.err.println("Authentication failed: " + e.getMessage());
                return;
            }

            // Deserialize the response body
            MOSIPAuthResponse response;
            try {
                String responseBody = responseEntity.getBody();
                ObjectMapper objectMapper = new ObjectMapper();
                response = objectMapper.readValue(responseBody, MOSIPAuthResponse.class);
            } catch (IOException e) {
                System.err.println("Failed to parse authentication response: " + e.getMessage());
                return;
            }

            // Handle Response Errors
            if (response.getErrors() != null && response.getErrors().length > 0) {
                for (Map<String, Object> error : response.getErrors()) {
                    System.out.println(error.get("errorCode") + " : " + error.get("errorMessage"));
                }
                System.exit(1);
            }

            // Print Response Status
            System.out.println("Response Status Code: " + response.getResponse().get("status"));

            // Decrypt Response
            String decryptedResponse = authenticator.decryptResponse(response.getResponse());
            System.out.println("Decrypted Response: " + decryptedResponse);

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            context.close();
        }
    }
}
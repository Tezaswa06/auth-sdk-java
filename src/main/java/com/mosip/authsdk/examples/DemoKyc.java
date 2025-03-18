package com.mosip.authsdk.examples;

import com.mosip.authsdk.authenticator.MosipAuthenticator;
import com.mosip.authsdk.config.MosipAuthProperties;
import com.mosip.authsdk.model.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collections;
import java.util.Map;

public class DemoKyc {
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
            demographicsData.setName(Collections.singletonList(new IdentityInfo("eng", "jevan mksm")));

            System.out.println("Demographics data: " + demographicsData);

            // Create KYC Authentication Request
            MOSIPAuthRequest authRequest = new MOSIPAuthRequest(
                    "mosip.identity.kyc",
                    "1.0",
                    "4370296312658178",
                    "VID",
                    "1234567890",
                    "2025-03-10T12:30:00Z",
                    "1.0",
                    "thumbprint_value_here",
                    "https://mosip.auth.com",
                    "Staging",
                    new MOSIPRequestedAuth(),
                    true,
                    "hmac_value_here",
                    "session_key_here",
                    "encrypted_request_data",
                    null
            );

            // Perform KYC Authentication
            MOSIPAuthResponse response = authenticator.kyc(authRequest);

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

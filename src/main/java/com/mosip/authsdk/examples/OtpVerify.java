package com.mosip.authsdk.examples;

import com.mosip.authsdk.authenticator.MosipAuthenticator;
import com.mosip.authsdk.config.MosipAuthProperties;
import com.mosip.authsdk.model.MOSIPAuthRequest;
import com.mosip.authsdk.model.MOSIPAuthResponse;
import com.mosip.authsdk.model.MOSIPRequestedAuth;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Map;

public class OtpVerify {
    public static void main(String[] args) {
        // Load Spring Context
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.scan("com.mosip.authsdk");
        context.refresh();

        try {
            // Load Configuration
            MosipAuthProperties config = context.getBean(MosipAuthProperties.class);
            MosipAuthenticator authenticator = context.getBean(MosipAuthenticator.class);

            // Create KYC Request with OTP
            MOSIPAuthRequest authRequest = new MOSIPAuthRequest(
                    "mosip.identity.auth",      // id
                    "1.0",                      // version
                    "4370296312658178",         // individualId
                    "VID",                      // individualIdType
                    "8300715076",               // transactionID
                    "2025-03-10T12:30:00Z",     // requestTime (ISO 8601 format)
                    "1.0",                      // specVersion
                    "thumbprint_value_here",    // thumbprint (retrieve dynamically)
                    "https://mosip.auth.com",   // domainUri
                    "Staging",                  // env
                    new MOSIPRequestedAuth(),   // requestedAuth (initialize a new instance)
                    true,                       // consentObtained
                    "hmac_value_here",          // requestHMAC
                    "session_key_here",         // requestSessionKey
                    "encrypted_request_data",   // request
                    null                        // metadata (optional)
            );

            // Perform KYC Authentication with OTP
            MOSIPAuthResponse response = authenticator.kyc(authRequest);

            // Handle Response
            if (response.getErrors() != null && response.getErrors().length > 0) {
                for (Map<String, Object> error : response.getErrors()) {
                    System.out.println(error.get("errorCode") + " : " + error.get("errorMessage"));
                }
                System.exit(1);
            }

            // Print Response Status Code
            System.out.println("Response Status Code: " + response.getResponse().get("status"));

            // Decrypt Response (Fixed - Passing Correct Data)
            String decryptedResponse = authenticator.decryptResponse(response.getResponse());
            System.out.println("Decrypted Response: " + decryptedResponse);

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensure Spring Context is closed
            context.close();
        }
    }
}

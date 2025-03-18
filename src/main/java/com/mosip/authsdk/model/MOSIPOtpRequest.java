package com.mosip.authsdk.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MOSIPOtpRequest extends MOSIPBaseRequest {

    @NotNull
    private List<String> otpChannel;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    // Constructor to initialize the fields and ensure proper validation of values
    public MOSIPOtpRequest(String idaAuthRequestId, String idaAuthVersion, String individualId,
                           String individualIdType, String transactionId, String timestamp,
                           List<String> otpChannel, Map<String, Object> metadata) {
        super(idaAuthRequestId, idaAuthVersion, individualId, individualIdType, transactionId, timestamp);
        setOtpChannel(otpChannel);  // Use setter for validation
        this.metadata = metadata;
    }

    // Setter with validation for otpChannel to allow only 'phone' and 'email'
    public void setOtpChannel(List<String> otpChannel) {
        for (String channel : otpChannel) {
            if (!channel.equals("phone") && !channel.equals("email")) {
                throw new IllegalArgumentException("Invalid OTP channel: " + channel + ". Allowed values are 'phone' and 'email'.");
            }
        }
        this.otpChannel = otpChannel;
    }

    // Placeholder for toJson method that could use Jackson ObjectMapper or similar
    public String toJson() {
        // Example implementation for converting to JSON using Jackson
        try {
            // Assuming ObjectMapper is available
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(this); // Convert object to JSON
        } catch (Exception e) {
            throw new RuntimeException("Error converting object to JSON", e);
        }
    }
}

package com.mosip.authsdk.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mosip.authsdk.exception.AuthenticatorCryptoException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MOSIPEncryptAuthRequest {

    @NotNull(message = "Biometrics cannot be null")
    @Valid
    private List<BiometricModel> biometrics;

    @Valid
    private DemographicsModel demographics;

    @NotNull(message = "OTP cannot be null")
    private String otp;

    @NotNull(message = "Timestamp cannot be null")
    private String timestamp;

    // Constructor to initialize fields
    public MOSIPEncryptAuthRequest(
            @NotNull String timestamp,
            @NotNull List<BiometricModel> biometrics,
            DemographicsModel demographics,
            @NotNull String otp
    ) {
        if (biometrics == null || otp == null || timestamp == null) {
            throw new IllegalArgumentException("Fields cannot be null");
        }
        this.timestamp = timestamp;
        this.biometrics = biometrics;
        this.demographics = demographics;
        this.otp = otp;
    }

    // You can also add validation here for fields if necessary
    public void validate() {
        if (this.biometrics == null || this.biometrics.isEmpty()) {
            throw new IllegalArgumentException("Biometrics list cannot be null or empty");
        }
        if (this.otp == null || this.otp.isEmpty()) {
            throw new IllegalArgumentException("OTP cannot be null or empty");
        }
        if (this.timestamp == null || this.timestamp.isEmpty()) {
            throw new IllegalArgumentException("Timestamp cannot be null or empty");
        }
    }
    public Map<String, String> encryptAuthData(MOSIPEncryptAuthRequest request) {
        try {
            String requestData = request.toJson();
            String encryptedData = encryptData(requestData);
            String sessionKey = Base64.getUrlEncoder().encodeToString(generateAESKey());
            String hmac = Base64.getUrlEncoder().encodeToString(generateHMAC(requestData, sessionKey));

            Map<String, String> encryptedMap = new HashMap<>();
            encryptedMap.put("request", encryptedData);
            encryptedMap.put("sessionKey", sessionKey);
            encryptedMap.put("hmac", hmac);

            return encryptedMap;
        } catch (Exception e) {
            log.error("Error encrypting auth data", e);
            throw new AuthenticatorCryptoException("AUT_CRY_003", "Failed to encrypt auth data");
        }
    }

    private byte[] generateHMAC(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }
    public String toJson() {
        // Implement the logic to convert the object to JSON string
        // For example, using Jackson ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert to JSON", e);
        }
    }
    private String encryptData(String data) {
        // Implement the logic to encrypt the data
        // This is a placeholder implementation
        return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }
    private byte[] generateAESKey() {
        // Implement the logic to generate AES key
        // This is a placeholder implementation
        byte[] key = new byte[16]; // 128-bit key
        new SecureRandom().nextBytes(key);
        return key;
    }
    private static final Logger log = LoggerFactory.getLogger(MOSIPEncryptAuthRequest.class);
}

package com.mosip.authsdk.authenticator;

import com.mosip.authsdk.config.MosipAuthProperties;
import com.mosip.authsdk.exception.AuthenticatorCryptoException;
import com.mosip.authsdk.exception.AuthenticatorException;
import com.mosip.authsdk.model.*;
import com.mosip.authsdk.util.CryptoUtil;
import com.mosip.authsdk.util.RestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class MosipAuthenticator {

    private static final Logger logger = LoggerFactory.getLogger(MosipAuthenticator.class);
    private final RestUtil restUtil;
    private final CryptoUtil cryptoUtil;
    private final MosipAuthProperties config;
    private final Map<String, String> idaAuthRequestIds;

    @Autowired
    public MosipAuthenticator(RestUtil restUtil, CryptoUtil cryptoUtil, MosipAuthProperties config) {
        this.restUtil = restUtil;
        this.cryptoUtil = cryptoUtil;
        this.config = config;

        this.idaAuthRequestIds = Map.of(
                "auth", config.getAuth().getIdaAuthRequestDemoId(),
                "kyc", config.getAuth().getIdaAuthRequestKycId(),
                "otp", config.getAuth().getIdaAuthRequestOtpId()
        );
    }

    /**
     * Generate OTP
     */
    public ResponseEntity<String> generateOtp(MOSIPOtpRequest request) {
        if (request.getOtpChannel().isEmpty()) {
            throw new AuthenticatorException("AUT_OTP_001", "No OTP channels selected");
        }
        logger.info("Generating OTP for individual ID: {}", request.getIndividualId());

        request.setTransactionID(generateTransactionId());
        String requestJson = request.toJson();
        String signature = cryptoUtil.signData(requestJson);

        Map<String, String> headers = new HashMap<>();
        headers.put("Signature", signature);

        // Fixed: Ensure method signature matches RestUtil's definition
        return restUtil.postRequest(buildAuthPath("otp"), headers, requestJson, null);
    }



    /**
     * Authenticate logic (for both auth & kyc)
     */
    private ResponseEntity<String> authenticateRequest(String controller, MOSIPAuthRequest request) {
        logger.info("Authenticating individual ID: {} using {}", request.getIndividualId(), controller);
        try {
            request.setTransactionID(generateTransactionId());
            String requestJson = request.toJson();
            String signature = cryptoUtil.signData(requestJson);

            Map<String, String> headers = new HashMap<>();
            headers.put("Signature", signature);

            return restUtil.postRequest(buildAuthPath(controller), headers, requestJson, null);
        } catch (Exception e) {
            logger.error("Authentication failed: {}", e.getMessage());
            throw new AuthenticatorException("AUT_AUTH_001", "Authentication process failed");
        }
    }

    /** Decrypt response */
    public String decryptResponse(Map<String, Object> responseBody) {
        Map<String, Object> response = (Map<String, Object>) responseBody.get("response");
        String sessionKeyB64 = (String) response.get("sessionKey");
        String identityB64 = (String) response.get("identity");

        return cryptoUtil.decryptData(sessionKeyB64, identityB64);
    }

    /** Create base request for authentication */
    private MOSIPBaseRequest getDefaultBaseRequest(String controller, String individualId, String individualIdType, String txnId) {
        String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        String transactionId = (txnId != null && !txnId.isEmpty()) ? txnId : generateTransactionId();

        return new MOSIPBaseRequest(
                idaAuthRequestIds.get(controller),
                config.getAuth().getIdaAuthVersion(),
                individualId,
                individualIdType,
                transactionId,
                timestamp
        );
    }

    /** Create default auth request */
    private MOSIPAuthRequest getDefaultAuthRequest(String controller, String individualId, String individualIdType, String txnId, boolean consentObtained) {
        MOSIPBaseRequest baseRequest = getDefaultBaseRequest(controller, individualId, individualIdType, txnId);

        return new MOSIPAuthRequest(
                baseRequest.getId(),
                baseRequest.getVersion(),
                baseRequest.getIndividualId(),
                baseRequest.getIndividualIdType(),
                baseRequest.getTransactionID(),
                baseRequest.getRequestTime(),
                config.getAuth().getIdaAuthVersion(),
                cryptoUtil.getCertThumbprint(),  // Fixed method call
                config.getAuthServer().getIdaAuthDomainUri(),
                config.getAuth().getIdaAuthEnv(),
                new MOSIPRequestedAuth(),
                consentObtained,
                "",
                "",
                "encrypted_request_data",
                null
        );
    }

    /**
     * Core Authentication logic (used for auth & kyc)
     */
    public ResponseEntity<String> authenticate(
            String controller,
            String individualId,
            String individualIdType,
            DemographicsModel demographicData,
            String otpValue,
            List<BiometricModel> biometrics,
            boolean consentObtained,
            String txnId
    ) {
        logger.info("Received Auth Request for demographic.");

        MOSIPAuthRequest authRequest = getDefaultAuthRequest(controller, individualId, individualIdType, txnId, consentObtained);

        // Encrypt request data
        MOSIPEncryptAuthRequest request = new MOSIPEncryptAuthRequest(
                authRequest.getRequestTime(),
                biometrics,
                demographicData,
                otpValue
        );

        try {
            Map<String, String> encryptedData = cryptoUtil.encryptAuthData(request); // Fixed method call
            authRequest.setRequest(encryptedData.get("request"));
            authRequest.setRequestSessionKey(encryptedData.get("sessionKey"));
            authRequest.setRequestHMAC(encryptedData.get("hmac"));
        } catch (AuthenticatorCryptoException exp) {
            logger.error("Failed to Encrypt Auth Data. Error Message: {}", exp.getMessage());
            throw exp;
        }

        String requestJson = authRequest.toJson();
        String signature = cryptoUtil.signData(requestJson);
        Map<String, String> headers = new HashMap<>();
        headers.put("Signature", signature);

        return restUtil.postRequest(buildAuthPath(controller), headers, requestJson, null);
    }

    /** Build request path */
    private String buildAuthPath(String controller) {
        try {
            // Ensure that required fields are not null
            String partnerMispLk = Optional.ofNullable(config.getAuth().getPartnerMispLk())
                    .orElseThrow(() -> new RuntimeException("PartnerMispLk is null"));
            String partnerId = Optional.ofNullable(config.getAuth().getPartnerId())
                    .orElseThrow(() -> new RuntimeException("PartnerId is null"));
            String partnerApikey = Optional.ofNullable(config.getAuth().getPartnerApikey())
                    .orElseThrow(() -> new RuntimeException("PartnerApikey is null"));

            // Properly encode URL components
            return String.join("/",
                    URLEncoder.encode(controller, StandardCharsets.UTF_8.toString()),
                    URLEncoder.encode(partnerMispLk, StandardCharsets.UTF_8.toString()),
                    URLEncoder.encode(partnerId, StandardCharsets.UTF_8.toString()),
                    URLEncoder.encode(partnerApikey, StandardCharsets.UTF_8.toString())
            );

        } catch (Exception e) {
            logger.error("Error encoding authentication path", e);
            throw new RuntimeException("Error encoding authentication path", e);
        }
    }
    /** Generate unique transaction ID */
    private String generateTransactionId() {
        return UUID.randomUUID().toString();
    }

    public MOSIPAuthResponse kyc(MOSIPAuthRequest authRequest) {
        ResponseEntity<String> response = authenticateRequest("kyc", authRequest);
        return new MOSIPAuthResponse(response.getBody());
    }
}

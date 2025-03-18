package com.mosip.authsdk.util;

import com.mosip.authsdk.exception.AuthenticatorCryptoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class RestUtil {
    private static final Logger logger = LoggerFactory.getLogger(RestUtil.class);
    private final RestTemplate restTemplate;
    private final String authServerUrl;
    private final HttpHeaders requestHeaders;

    public RestUtil(
            @Value("${auth.server.url}") String authServerUrl,
            @Value("${authorization.header.constant}") String authorizationHeaderConstant
    ) {
        this.restTemplate = new RestTemplate();
        this.authServerUrl = authServerUrl;
        this.requestHeaders = new HttpHeaders();
        this.requestHeaders.set("Authorization", authorizationHeaderConstant);
        this.requestHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    /**
     * Sends a GET request with optional headers, cookies, and data.
     */
    public ResponseEntity<String> getRequest(String pathParams, Map<String, String> headers, String data, Map<String, String> cookies) {
        String serverUrl = buildUrl(pathParams);
        HttpHeaders httpHeaders = buildHeaders(headers, cookies);

        logger.info("Got <GET> Request for URL: {}", serverUrl);

        HttpEntity<String> requestEntity = new HttpEntity<>(data, httpHeaders);
        return restTemplate.exchange(serverUrl, HttpMethod.GET, requestEntity, String.class);
    }

    /**
     * Sends a POST request with optional headers, cookies, and data.
     */
    public ResponseEntity<String> postRequest(String pathParams, Map<String, String> additionalHeaders, String data, Map<String, String> cookies) {
        String serverUrl = buildUrl(pathParams);
        HttpHeaders httpHeaders = buildHeaders(additionalHeaders, cookies);

        logger.info("Got <POST> Request for URL: {}", serverUrl);
        logger.debug("Final request route = {}", serverUrl);
        logger.debug("Request Headers = {}", httpHeaders);

        HttpEntity<String> requestEntity = new HttpEntity<>(data, httpHeaders);
        return restTemplate.exchange(serverUrl, HttpMethod.POST, requestEntity, String.class);
    }

    /**
     * Builds the URL with the path parameters, ensuring proper concatenation.
     */
    private String buildUrl(String pathParams) {
        if (pathParams == null || pathParams.isEmpty()) {
            return authServerUrl;
        }
        return authServerUrl.endsWith("/") ? authServerUrl + pathParams : authServerUrl + "/" + pathParams;
    }

    /**
     * Builds headers for the request, including authorization and custom headers.
     */
    private HttpHeaders buildHeaders(Map<String, String> additionalHeaders, Map<String, String> cookies) {
        HttpHeaders headers = new HttpHeaders();
        headers.addAll(this.requestHeaders);

        if (additionalHeaders != null) {
            additionalHeaders.forEach(headers::set);
        }

        if (cookies != null) {
            cookies.forEach((key, value) -> headers.add(HttpHeaders.COOKIE, key + "=" + value));
        }

        return headers;
    }

    /**
     * Encrypts the request data using AES encryption and asymmetric encryption for the AES key.
     */
    public String encryptData(String data, CryptoUtil cryptoUtil) {
        try {
            // Encrypt the data using AES
            String encryptedData = cryptoUtil.encryptData(data);
            // Log the encrypted data for debugging
            logger.info("Encrypted Data: {}", encryptedData);
            return encryptedData;
        } catch (Exception e) {
            logger.error("Error encrypting data", e);
            throw new AuthenticatorCryptoException("AUT_CRY_003", "Failed to encrypt data");
        }
    }

    /**
     * Decrypts the response data using AES decryption and asymmetric decryption for the AES key.
     */
    public String decryptData(String encryptedData, String encryptedKey, CryptoUtil cryptoUtil) {
        try {
            // Decrypt the data using AES
            String decryptedData = cryptoUtil.decryptData(encryptedData, encryptedKey);
            // Log the decrypted data for debugging
            logger.info("Decrypted Data: {}", decryptedData);
            return decryptedData;
        } catch (Exception e) {
            logger.error("Error decrypting data", e);
            throw new AuthenticatorCryptoException("AUT_CRY_004", "Failed to decrypt data");
        }
    }

    /**
     * Signs the request data using the private key and the specified algorithm.
     */
    public String signRequestData(String data, CryptoUtil cryptoUtil) {
        try {
            String signedData = cryptoUtil.signData(data);
            logger.info("Signed Data: {}", signedData);
            return signedData;
        } catch (Exception e) {
            logger.error("Error signing data", e);
            throw new AuthenticatorCryptoException("AUT_CRY_005", "Failed to sign data");
        }
    }
}

package com.mosip.authsdk.util;

import com.mosip.authsdk.exception.AuthenticatorCryptoException;
import com.mosip.authsdk.model.MOSIPEncryptAuthRequest;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CryptoUtil {

    @Value("${encrypt.cert.path}")
    private String encryptCertPath;

    @Value("${decrypt.p12.path}")
    private String decryptP12Path;

    @Value("${decrypt.p12.password}")
    private String decryptP12Password;

    @Value("${sign.p12.path}")
    private String signP12Path;

    @Value("${sign.p12.password}")
    private String signP12Password;

    @Value("${crypto.algorithm}")
    private String algorithm;

    @Value("${symmetric.key.size}")
    private int symmetricKeySize;

    @Value("${symmetric.nonce.size}")
    private int symmetricNonceSize;

    @Value("${symmetric.gcm.tag.size}")
    private int symmetricGcmTagSize;

    private X509Certificate encryptCert;
    private PrivateKey decryptPrivateKey;
    private PrivateKey signPrivateKey;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @PostConstruct
    public void init() {
        this.encryptCert = loadCertificate(encryptCertPath);
        this.decryptPrivateKey = loadPrivateKey(decryptP12Path, decryptP12Password);
        this.signPrivateKey = loadPrivateKey(signP12Path, signP12Password);
    }

    private X509Certificate loadCertificate(String certPath) {
        try (FileInputStream fis = new FileInputStream(certPath)) {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) factory.generateCertificate(fis);
        } catch (Exception e) {
            log.error("Error loading certificate from path: {}", certPath, e);
            throw new AuthenticatorCryptoException("AUT_CRY_001", "Failed to load certificate");
        }
    }

    private PrivateKey loadPrivateKey(String p12Path, String password) {
        try (FileInputStream fis = new FileInputStream(p12Path)) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(fis, password.toCharArray());

            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (keyStore.isKeyEntry(alias)) {
                    return (PrivateKey) keyStore.getKey(alias, password.toCharArray());
                }
            }
        } catch (Exception e) {
            log.error("Error loading private key from path: {}", p12Path, e);
            throw new AuthenticatorCryptoException("AUT_CRY_002", "Failed to load private key");
        }
        return null;
    }

    public String encryptData(String data) {
        try {
            byte[] aesKey = generateAESKey();
            byte[] encryptedData = symmetricEncrypt(data.getBytes(StandardCharsets.UTF_8), aesKey);
            byte[] encryptedKey = asymmetricEncrypt(aesKey);

            return Base64.getUrlEncoder().encodeToString(encryptedData) + ","
                    + Base64.getUrlEncoder().encodeToString(encryptedKey);
        } catch (Exception e) {
            log.error("Error encrypting data", e);
            throw new AuthenticatorCryptoException("AUT_CRY_003", "Failed to encrypt data");
        }
    }

    public String decryptData(String encryptedData, String encryptedKey) {
        try {
            byte[] decodedKey = Base64.getUrlDecoder().decode(encryptedKey);
            byte[] aesKey = asymmetricDecrypt(decodedKey);

            byte[] decodedData = Base64.getUrlDecoder().decode(encryptedData);
            return new String(symmetricDecrypt(decodedData, aesKey), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error decrypting data", e);
            throw new AuthenticatorCryptoException("AUT_CRY_004", "Failed to decrypt data");
        }
    }

    private byte[] generateAESKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(symmetricKeySize);
            return keyGen.generateKey().getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("AES key generation failed", e);
        }
    }

    private byte[] asymmetricEncrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, encryptCert.getPublicKey());
        return cipher.doFinal(data);
    }

    private byte[] asymmetricDecrypt(byte[] encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, decryptPrivateKey);
        return cipher.doFinal(encryptedData);
    }

    private byte[] symmetricEncrypt(byte[] data, byte[] key) throws Exception {
        byte[] iv = new byte[symmetricNonceSize];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(symmetricGcmTagSize * 8, iv);

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
        return cipher.doFinal(data);
    }

    private byte[] symmetricDecrypt(byte[] data, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(symmetricGcmTagSize * 8, data);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
        return cipher.doFinal(data);
    }

    public String signData(String data) {
        try {
            Signature signature = Signature.getInstance(algorithm);
            signature.initSign(signPrivateKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            log.error("Error signing data", e);
            throw new AuthenticatorCryptoException("AUT_CRY_005", "Failed to sign data");
        }
    }
    /** Get Certificate Thumbprint */
    public String getCertThumbprint() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(encryptCert.getEncoded());
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            log.error("Error generating certificate thumbprint", e);
            throw new AuthenticatorCryptoException("AUT_CRY_006", "Failed to generate certificate thumbprint");
        }
    }
    // Add this method to CryptoUtil class
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

}

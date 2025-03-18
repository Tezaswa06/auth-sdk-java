package com.mosip.authsdk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "mosip")
public class MosipAuthProperties {

    private Auth auth;
    private AuthServer authServer;
    private Crypto crypto;
    private Logging logging;

    @Getter
    @Setter
    public static class Auth {
        private String timestampFormat;
        private String idaAuthVersion;
        private String idaAuthRequestDemoId;
        private String idaAuthRequestKycId;
        private String idaAuthRequestOtpId;
        private String idaAuthEnv;
        private String authorizationHeaderConstant;
        private String partnerApikey;
        private String partnerMispLk;
        private String partnerId;
    }

    @Getter
    @Setter
    public static class AuthServer {
        private String idaAuthDomainUri;
        private String idaAuthUrl;
    }

    @Getter
    @Setter
    public static class Crypto {
        private Encrypt encrypt;
        private Signature signature;

        @Getter
        @Setter
        public static class Encrypt {
            private int symmetricKeySize;
            private int symmetricNonceSize;
            private int symmetricGcmTagSize;
            private String encryptCertPath;
            private String decryptP12FilePath;
            private String decryptP12FilePassword;
        }

        @Getter
        @Setter
        public static class Signature {
            private String algorithm;
            private String signP12FilePath;
            private String signP12FilePassword;
        }
    }

    @Getter
    @Setter
    public static class Logging {
        private String logFilePath;
        private String logFormat;
        private String logLevel;
    }
}

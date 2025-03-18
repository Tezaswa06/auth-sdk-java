package com.mosip.authsdk.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class MOSIPAuthRequest extends MOSIPBaseRequest {

    @NotNull
    private String id;

    @NotNull
    private String version;

    @NotNull
    private String individualId;

    @NotNull
    private String individualIdType;

    @NotNull
    private String transactionID;

    @NotNull
    private String requestTime;

    @NotNull
    private String specVersion;

    @NotNull
    private String thumbprint;

    @NotNull
    private String domainUri;

    @NotNull
    private String env;

    @JsonProperty("requestedAuth")
    private MOSIPRequestedAuth requestedAuth = new MOSIPRequestedAuth();

    @NotNull
    private boolean consentObtained;

    @NotNull
    private String requestHMAC;

    @NotNull
    private String requestSessionKey;

    @NotNull
    private String request;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    // Constructor to initialize fields
    public MOSIPAuthRequest(
            @NotNull String id,
            @NotNull String version,
            @NotNull String individualId,
            @NotNull String individualIdType,
            @NotNull String transactionID,
            @NotNull String requestTime,
            @NotNull String specVersion,
            @NotNull String thumbprint,
            @NotNull String domainUri,
            @NotNull String env,
            MOSIPRequestedAuth requestedAuth,
            @NotNull boolean consentObtained,
            @NotNull String requestHMAC,
            @NotNull String requestSessionKey,
            @NotNull String request,
            Map<String, Object> metadata
    ) {
        super(id, version, individualId, individualIdType, transactionID, requestTime);
        this.specVersion = specVersion;
        this.thumbprint = thumbprint;
        this.domainUri = domainUri;
        this.env = env;
        this.requestedAuth = requestedAuth;
        this.consentObtained = consentObtained;
        this.requestHMAC = requestHMAC;
        this.requestSessionKey = requestSessionKey;
        this.request = request;
        this.metadata = metadata;
    }

    // Placeholder for converting the object to JSON
    public String toJson() {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(this); // Convert object to JSON
        } catch (Exception e) {
            throw new RuntimeException("Error converting object to JSON", e);
        }
    }
}

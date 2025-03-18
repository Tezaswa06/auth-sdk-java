package com.mosip.authsdk.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MOSIPOtpResponse {
    @JsonProperty("id")
    private String id;

    @JsonProperty("version")
    private String version;

    @JsonProperty("responseTime")
    private String responseTime;

    @JsonProperty("transactionID")
    private String transactionID;

    @JsonProperty("response")
    private Map<String, Object> response;

    @JsonProperty("errors")
    private Map<String, Object>[] errors;
}

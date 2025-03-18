package com.mosip.authsdk.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MOSIPBaseRequest {
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

    public MOSIPBaseRequest(String idaAuthRequestId, String idaAuthVersion, String individualId, String individualIdType, String transactionId, String timestamp) {
    }
}

package com.mosip.authsdk.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BiometricModelDataDigitalIdField {
    @NotNull
    private String serialNo;

    @NotNull
    private String make;

    @NotNull
    private String model;

    @NotNull
    private String type;

    @NotNull
    private String deviceSubType;

    @NotNull
    private String deviceProvider;

    @NotNull
    private String dp;

    @NotNull
    private String dpId;

    @NotNull
    private String deviceProviderId;

    @NotNull
    private String dateTime;

}

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
public class BiometricModel {
    @NotNull
    private BiometricModelDataDigitalIdField data;

    @NotNull
    private String hash;

    @NotNull
    private String sessionKey;

    @NotNull
    private String specVersion;

    @NotNull
    private String thumbprint;

}

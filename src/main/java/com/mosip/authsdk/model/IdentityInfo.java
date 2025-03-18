package com.mosip.authsdk.model;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class IdentityInfo {
    @NotNull
    private String language;

    @NotNull
    private String value;
}

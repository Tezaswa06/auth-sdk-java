package com.mosip.authsdk.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MOSIPRequestedAuth {
    private boolean demo = false;
    private boolean pin = false;
    private boolean otp = false;
    private boolean bio = false;

}

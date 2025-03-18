package com.mosip.authsdk.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DemographicsModel {
    private String age = "";
    private String dob = "";

    @Valid
    private List<IdentityInfo> name = new ArrayList<>();

    @JsonProperty("dob_type")
    private List<IdentityInfo> dobType = new ArrayList<>();

    private List<IdentityInfo> gender = new ArrayList<>();

    @JsonProperty("phone_number")
    private String phoneNumber = "";

    @JsonProperty("email_id")
    private String emailId = "";

    @JsonProperty("address_line1")
    private List<IdentityInfo> addressLine1 = new ArrayList<>();

    @JsonProperty("address_line2")
    private List<IdentityInfo> addressLine2 = new ArrayList<>();

    @JsonProperty("address_line3")
    private List<IdentityInfo> addressLine3 = new ArrayList<>();

    private List<IdentityInfo> location1 = new ArrayList<>();
    private List<IdentityInfo> location2 = new ArrayList<>();
    private List<IdentityInfo> location3 = new ArrayList<>();

    @JsonProperty("postal_code")
    private String postalCode = "";

    @JsonProperty("full_address")
    private List<IdentityInfo> fullAddress = new ArrayList<>();

    private Optional<Map<String, Object>> metadata = Optional.empty();

}

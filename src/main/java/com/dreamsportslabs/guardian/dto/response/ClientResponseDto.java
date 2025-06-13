package com.dreamsportslabs.guardian.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientResponseDto {
  @JsonProperty("client_id")
  private String clientId;

  @JsonProperty("client_name")
  private String clientName;

  @JsonProperty("client_secret")
  private String clientSecret;

  @JsonProperty("client_uri")
  private String clientUri;

  private List<String> contacts;

  @JsonProperty("grant_types")
  private List<String> grantTypes;

  @JsonProperty("logo_uri")
  private String logoUri;

  @JsonProperty("policy_uri")
  private String policyUri;

  @JsonProperty("redirect_uris")
  private List<String> redirectUris;

  @JsonProperty("response_types")
  private List<String> responseTypes;

  @JsonProperty("skip_consent")
  private Boolean skipConsent;
}

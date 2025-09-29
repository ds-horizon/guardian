package com.dreamsportslabs.guardian.dto.response;

import com.dreamsportslabs.guardian.constant.OidcGrantType;
import com.dreamsportslabs.guardian.constant.OidcResponseType;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
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

  @JsonProperty("contacts")
  private List<String> contacts;

  @JsonProperty("grant_types")
  private List<OidcGrantType> grantTypes;

  @JsonProperty("logo_uri")
  private String logoUri;

  @JsonProperty("policy_uri")
  private String policyUri;

  @JsonProperty("redirect_uris")
  private List<String> redirectUris;

  @JsonProperty("response_types")
  private List<OidcResponseType> responseTypes;

  @JsonProperty("client_type")
  private String clientType;

  @JsonProperty("is_default")
  private Boolean isDefault;
}

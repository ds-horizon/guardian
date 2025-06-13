package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.constant.OidcGrantTypes;
import com.dreamsportslabs.guardian.constant.OidcResponseTypes;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class CreateClientRequestDto {
  @JsonProperty("client_name")
  private String clientName;

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
  private Boolean skipConsent = false;

  @JsonProperty("scopes")
  private List<String> scopes;

  public void validate() {
    if (StringUtils.isBlank(clientName)) {
      throw INVALID_REQUEST.getCustomException("Client name is required");
    }

    if (grantTypes == null || grantTypes.isEmpty()) {
      throw INVALID_REQUEST.getCustomException("Grant types are required");
    }

    if (redirectUris == null || redirectUris.isEmpty()) {
      throw INVALID_REQUEST.getCustomException("Redirect URIs are required");
    }

    if (responseTypes == null || responseTypes.isEmpty()) {
      throw INVALID_REQUEST.getCustomException("Response types are required");
    }

    // Validate grant types
    List<String> validGrantTypes =
        Arrays.stream(OidcGrantTypes.values()).map(OidcGrantTypes::getValue).toList();
    for (String grantType : grantTypes) {
      if (!validGrantTypes.contains(grantType)) {
        throw INVALID_REQUEST.getCustomException("Invalid grant type: " + grantType);
      }
    }

    // Validate response types
    List<String> validResponseTypes =
        Arrays.stream(OidcResponseTypes.values()).map(OidcResponseTypes::getValue).toList();
    for (String responseType : responseTypes) {
      if (!validResponseTypes.contains(responseType)) {
        throw INVALID_REQUEST.getCustomException("Invalid response type: " + responseType);
      }
    }
  }
}

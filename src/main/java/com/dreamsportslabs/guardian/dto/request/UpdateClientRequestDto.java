package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.constant.OidcGrantType;
import com.dreamsportslabs.guardian.constant.OidcResponseType;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class UpdateClientRequestDto {
  @JsonProperty("client_name")
  private String clientName;

  @JsonProperty("client_uri")
  private String clientUri;

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

  @JsonProperty("skip_consent")
  private Boolean skipConsent;

  public void validate() {
    if (clientName != null && StringUtils.isBlank(clientName)) {
      throw INVALID_REQUEST.getCustomException("Client name cannot be blank");
    }

    if (grantTypes != null && grantTypes.isEmpty()) {
      throw INVALID_REQUEST.getCustomException("Grant types cannot be empty");
    }

    if (redirectUris != null && redirectUris.isEmpty()) {
      throw INVALID_REQUEST.getCustomException("Redirect URIs cannot be empty");
    }

    if (responseTypes != null && responseTypes.isEmpty()) {
      throw INVALID_REQUEST.getCustomException("Response types cannot be empty");
    }
  }
}

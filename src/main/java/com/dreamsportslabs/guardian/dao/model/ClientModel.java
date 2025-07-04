package com.dreamsportslabs.guardian.dao.model;

import com.dreamsportslabs.guardian.constant.OidcGrantType;
import com.dreamsportslabs.guardian.constant.OidcResponseType;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Setter
@Jacksonized
public class ClientModel {
  private String tenantId;
  private String clientId;
  private String clientName;
  private String clientSecret;
  private String clientUri;
  private List<String> contacts;
  private List<OidcGrantType> grantTypes;
  private String logoUri;
  private String policyUri;
  private List<String> redirectUris;
  private List<OidcResponseType> responseTypes;
  private Boolean skipConsent;
}

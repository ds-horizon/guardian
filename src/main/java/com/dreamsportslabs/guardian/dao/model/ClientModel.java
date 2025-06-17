package com.dreamsportslabs.guardian.dao.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientModel {
  private String tenantId;
  private String clientId;
  private String clientName;
  private String clientSecret;
  private String clientUri;
  private List<String> contacts;
  private List<String> grantTypes;
  private String logoUri;
  private String policyUri;
  private List<String> redirectUris;
  private List<String> responseTypes;
  private Boolean skipConsent;
}

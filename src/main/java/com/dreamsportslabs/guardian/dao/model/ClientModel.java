package com.dreamsportslabs.guardian.dao.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
<<<<<<< HEAD
import lombok.Setter;
=======
>>>>>>> d43d5df (chore: address PR comments)
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
<<<<<<< HEAD
@Setter
=======
>>>>>>> d43d5df (chore: address PR comments)
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

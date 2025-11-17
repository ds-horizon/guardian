package com.dreamsportslabs.guardian.dao.model;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class CredentialModel {
  private Long id;
  private String tenantId;
  private String clientId;
  private String userId;
  private String credentialId;
  private String publicKey;
  private String bindingType; // 'webauthn' or 'appkey'
  private Integer alg;
  private Long signCount;
  private String aaguid;
  private Boolean isActive;
}

package com.dreamsportslabs.guardian.dao.model;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class ClientScopeModel {
  private String tenantId;
  private String scope;
  private String clientId;
  private Boolean isDefault;
}

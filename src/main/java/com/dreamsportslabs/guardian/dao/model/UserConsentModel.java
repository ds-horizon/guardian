package com.dreamsportslabs.guardian.dao.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserConsentModel {
  private Integer id;
  private String tenantId;
  private String clientId;
  private String userId;
  private String scope;
}

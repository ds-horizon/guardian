package com.dreamsportslabs.guardian.dao.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@Getter
public class ScopeModel {
  private String name;
  private String displayName;
  private String description;
  private List<String> claims;
  private String tenantId;
  private String iconUrl;
  private Boolean isOidc;
}

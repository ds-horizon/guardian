package com.dreamsportslabs.guardian.dao.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScopeModel {
  private String name;
  private String displayName;
  private String description;
  private List<String> claims;
  private String tenantId;
  private String iconUrl;
  private Boolean isOidc;
}

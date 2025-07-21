package com.dreamsportslabs.guardian.dao.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFlowBlockModel {
  private String tenantId;
  private String userIdentifier;
  private String flowName;
  private String reason;
  private Long unblockedAt;
  private boolean isActive;
}

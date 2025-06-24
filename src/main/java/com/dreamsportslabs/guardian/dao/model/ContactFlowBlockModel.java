package com.dreamsportslabs.guardian.dao.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactFlowBlockModel {
  private String tenantId;
  private String contact;
  private String flowName;
  private String reason;
  private String operator;
  private Long unblockedAt;
  private boolean isActive;
}

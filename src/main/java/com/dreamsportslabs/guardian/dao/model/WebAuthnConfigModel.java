package com.dreamsportslabs.guardian.dao.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class WebAuthnConfigModel {
  private String tenantId;
  private String clientId;
  private String rpId;
  private List<String> allowedWebOrigins;
  private List<String> allowedAlgorithms;
  private String aaguidPolicyMode; // 'allowlist', 'mds_enforced', 'any'
  private List<String> allowedAaguids;
  private List<String> blockedAaguids;
  private Boolean requireUvEnrollment;
  private Boolean requireUvAuth;
  private List<String> allowedTransports;
  private Boolean requireDeviceBound;
}

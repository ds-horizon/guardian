package com.dreamsportslabs.guardian.dao.model;

import com.dreamsportslabs.guardian.dto.request.DeviceMetadata;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class WebAuthnStateModel {
  private String state;
  private String tenantId;
  private String clientId;
  private String userId;
  private String challenge;
  private String type; // "assert" or "enroll"
  private DeviceMetadata deviceMetadata;
  private Map<String, Object> additionalInfo;
  private long expiry;
}

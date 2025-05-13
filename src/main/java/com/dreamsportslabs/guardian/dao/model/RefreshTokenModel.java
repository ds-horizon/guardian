package com.dreamsportslabs.guardian.dao.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshTokenModel {
  private String tenantId;
  private String userId;
  private String refreshToken;
  private long refreshTokenExp;
  private String location;
  private String deviceName;
  private String ip;
  private String source;
}

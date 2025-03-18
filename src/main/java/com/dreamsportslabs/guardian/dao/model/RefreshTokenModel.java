package com.dreamsportslabs.guardian.dao.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshTokenModel {
  String tenantId;
  String userId;
  String refreshToken;
  long refreshTokenExp;
  String location;
  String deviceName;
  String ip;
  String source;
}

package com.dreamsportslabs.guardian.dao.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CookieModel {
  String tenantId;
  String userId;
  String cookieName;
  String cookieValue;
  long cookieExp;
  String domain;
  String path;
  String sameSite;
  boolean isActive;
  String source;
  String deviceName;
  String location;
  String ip;
}

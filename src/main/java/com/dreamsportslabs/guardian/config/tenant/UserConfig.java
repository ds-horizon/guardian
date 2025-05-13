package com.dreamsportslabs.guardian.config.tenant;

import lombok.Data;

@Data
public class UserConfig {
  private String host;
  private int port;
  private Boolean isSslEnabled;
  private String createUserPath;
  private String getUserPath;
  private String authenticateUserPath;
  private String addProviderPath;
}

package com.dreamsportslabs.guardian.config.tenant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserConfig {
  private String host;
  private int port;
  private Boolean isSslEnabled;
  private String createUserPath;
  private String getUserPath;
  private String authenticateUserPath;
  private String addProviderPath;
}

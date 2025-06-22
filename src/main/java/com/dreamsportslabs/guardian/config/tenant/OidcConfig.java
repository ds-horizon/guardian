package com.dreamsportslabs.guardian.config.tenant;

import lombok.Data;

@Data
public class OidcConfig {
  private String loginPageUri;
  private String consentPageUri;
  private Integer authorizeTtl;
}

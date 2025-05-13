package com.dreamsportslabs.guardian.config.tenant;

import java.util.List;
import lombok.Data;

@Data
public class TokenConfig {
  private String algorithm;
  private String issuer;
  private Integer accessTokenExpiry;
  private Integer refreshTokenExpiry;
  private Integer idTokenExpiry;
  private List<String> idTokenClaims;
  private List<RsaKey> rsaKeys;
}

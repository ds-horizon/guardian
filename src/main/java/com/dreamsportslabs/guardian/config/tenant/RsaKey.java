package com.dreamsportslabs.guardian.config.tenant;

import lombok.Data;

@Data
public class RsaKey {
  private String publicKey;
  private String privateKey;
  private String kid;
  private Boolean current = false;
}

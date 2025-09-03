package com.dreamsportslabs.guardian.config.tenant;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class GuestConfig {
  @JsonProperty("shared_secret_key")
  private String sharedSecretKey;

  @JsonProperty("is_encrypted")
  private Boolean isEncrypted;

  @JsonProperty("allowed_scopes")
  private List<String> allowedScopes;
}

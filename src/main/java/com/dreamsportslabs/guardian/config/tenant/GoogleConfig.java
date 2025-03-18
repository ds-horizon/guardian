package com.dreamsportslabs.guardian.config.tenant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleConfig {
  private String clientId;
  private String clientSecret;
}

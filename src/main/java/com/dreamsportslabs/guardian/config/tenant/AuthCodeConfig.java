package com.dreamsportslabs.guardian.config.tenant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthCodeConfig {
  private int length;
  private int ttl;
}

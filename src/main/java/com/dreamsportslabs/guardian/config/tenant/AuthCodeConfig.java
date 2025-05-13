package com.dreamsportslabs.guardian.config.tenant;

import lombok.Data;

@Data
public class AuthCodeConfig {
  private int length;
  private int ttl;
}

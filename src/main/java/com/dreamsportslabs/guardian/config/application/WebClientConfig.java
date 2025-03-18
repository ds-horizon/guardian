package com.dreamsportslabs.guardian.config.application;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WebClientConfig {
  private Integer maxPoolSize;
  private Integer keepAliveTimeout;
  private Integer idleTimeout;
  private Boolean keepAlive;
}

package com.dreamsportslabs.guardian.config.application;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RedisConfig {
  private String host;
  private int port = 6379;
  private int maxPoolSize = 10;
  private String type;
}

package com.dreamsportslabs.guardian.config.application;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RedisConfig {
  /** Redis host. */
  @NotNull private String host;

  /** Redis port. */
  private int port = 6379; // default Redis port

  /** Maximum pool size for Redis connections. */
  private int maxPoolSize = 10; // default value

  private String type;
}

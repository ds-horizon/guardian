package com.dreamsportslabs.guardian.config.application;

import io.vertx.core.http.HttpServerOptions;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Config {
  @NotNull private HttpServerOptions httpServerOptions;
  @NotNull @Valid private MySQLConfig mySQLConfig;
  @NotNull @Valid private WebClientConfig webClientConfig;
  @NotNull @Valid private RedisConfig redisConfig;
}

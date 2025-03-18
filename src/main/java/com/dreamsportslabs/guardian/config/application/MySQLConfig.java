package com.dreamsportslabs.guardian.config.application;

import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MySQLConfig {
  @NotNull @Valid private MySQLBaseConfig readerConfig;
  @NotNull @Valid private MySQLBaseConfig writerConfig;

  @Data
  @NoArgsConstructor
  public static class MySQLBaseConfig {
    @NotNull private MySQLConnectOptions connectOptions;
    @NotNull private PoolOptions poolOptions;
    @NotNull @Positive private Integer retryCount;
  }
}

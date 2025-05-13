package com.dreamsportslabs.guardian.config.application;

import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MySQLConfig {
  private MySQLBaseConfig readerConfig;
  private MySQLBaseConfig writerConfig;

  @Data
  @NoArgsConstructor
  public static class MySQLBaseConfig {
    private MySQLConnectOptions connectOptions;
    private PoolOptions poolOptions;
    private Integer retryCount;
  }
}

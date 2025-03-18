package com.dreamsportslabs.guardian.client.impl;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class MysqlClientImpl implements MysqlClient {
  private final Vertx vertx;
  private MySQLPool masterClient;
  private MySQLPool slaveClient;
  private JsonObject config;

  public Completable rxConnect(JsonObject config) {
    this.config = config;
    this.createMasterSlavePool();
    return Completable.complete();
  }

  @Override
  public Completable rxClose() {
    return this.masterClient.rxClose().andThen(this.slaveClient.rxClose());
  }

  private void createMasterSlavePool() {
    MySQLConnectOptions masterConnectOptions =
        new MySQLConnectOptions(
            JsonUtils.getJsonObjectFromNestedJson(this.config, "writerConfig.connectOptions"));

    MySQLConnectOptions slaveConnectOptions =
        new MySQLConnectOptions(
            JsonUtils.getJsonObjectFromNestedJson(this.config, "readerConfig.connectOptions"));

    PoolOptions masterPoolOptions =
        new PoolOptions(
            JsonUtils.getJsonObjectFromNestedJson(this.config, "writerConfig.poolOptions"));

    PoolOptions slavePoolOptions =
        new PoolOptions(
            JsonUtils.getJsonObjectFromNestedJson(this.config, "readerConfig.poolOptions"));

    this.masterClient = MySQLPool.pool(this.vertx, masterConnectOptions, masterPoolOptions);
    this.slaveClient = MySQLPool.pool(this.vertx, slaveConnectOptions, slavePoolOptions);
  }
}

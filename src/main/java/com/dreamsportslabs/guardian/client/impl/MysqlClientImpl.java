package com.dreamsportslabs.guardian.client.impl;

import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_DATABASE;
import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_PASSWORD;
import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_READER_HOST;
import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_READER_MAX_POOL_SIZE;
import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_USER;
import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_WRITER_HOST;
import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_WRITER_MAX_POOL_SIZE;

import com.dreamsportslabs.guardian.client.MysqlClient;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MysqlClientImpl implements MysqlClient {
  private final Vertx vertx;
  private final JsonObject config;
  private MySQLPool writerPool;
  private MySQLPool readerPool;

  public MysqlClientImpl(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.config = config;
    createConnectionPool();
  }

  public Completable rxConnect() {
    return Completable.complete();
  }

  @Override
  public Completable rxClose() {
    return this.writerPool.rxClose().andThen(this.readerPool.rxClose());
  }

  private void createConnectionPool() {
    log.info(config.toString());
    MySQLConnectOptions writerConnectOptions =
        new MySQLConnectOptions()
            .setHost(this.config.getString(MYSQL_WRITER_HOST))
            .setUser(this.config.getString(MYSQL_USER))
            .setPassword(this.config.getString(MYSQL_PASSWORD))
            .setDatabase(this.config.getString(MYSQL_DATABASE));
    PoolOptions writerPoolOptions =
        new PoolOptions()
            .setMaxSize(Integer.parseInt(this.config.getString(MYSQL_WRITER_MAX_POOL_SIZE)));

    MySQLConnectOptions readerConnectOptions =
        new MySQLConnectOptions()
            .setHost(this.config.getString(MYSQL_READER_HOST))
            .setUser(this.config.getString(MYSQL_USER))
            .setPassword(this.config.getString(MYSQL_PASSWORD))
            .setDatabase(this.config.getString(MYSQL_DATABASE));
    PoolOptions readerPoolOptions =
        new PoolOptions()
            .setMaxSize(Integer.parseInt(this.config.getString(MYSQL_READER_MAX_POOL_SIZE)));

    this.writerPool = MySQLPool.pool(this.vertx, writerConnectOptions, writerPoolOptions);
    this.readerPool = MySQLPool.pool(this.vertx, readerConnectOptions, readerPoolOptions);
  }

  public MySQLPool getWriterPool() {
    return this.writerPool;
  }

  public MySQLPool getReaderPool() {
    return this.readerPool;
  }
}

package com.dreamsportslabs.guardian.verticle;

import static com.dreamsportslabs.guardian.constant.Constants.HTTP_CONNECT_TIMEOUT;
import static com.dreamsportslabs.guardian.constant.Constants.HTTP_READ_TIMEOUT;
import static com.dreamsportslabs.guardian.constant.Constants.HTTP_WRITE_TIMEOUT;
import static com.dreamsportslabs.guardian.constant.Constants.PORT;
import static com.dreamsportslabs.guardian.constant.Constants.REDIS_HOST;
import static com.dreamsportslabs.guardian.constant.Constants.REDIS_PORT;
import static com.dreamsportslabs.guardian.constant.Constants.REDIS_TYPE;
import static com.dreamsportslabs.guardian.constant.Constants.TENANT_CONFIG_REFRESH_INTERVAL;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.client.impl.MysqlClientImpl;
import com.dreamsportslabs.guardian.registry.Registry;
import com.dreamsportslabs.guardian.utils.ConfigUtil;
import com.dreamsportslabs.guardian.utils.SharedDataUtils;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.redis.client.RedisClientType;
import io.vertx.redis.client.RedisOptions;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.ext.web.client.WebClient;
import io.vertx.rxjava3.redis.client.Redis;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainVerticle extends AbstractVerticle {
  private Redis redisClient;
  private WebClient webClient;
  private MysqlClient mysqlClient;
  private JsonObject config;

  @Override
  public Completable rxStart() {
    SharedDataUtils.put(vertx.getDelegate(), new Registry());
    return ConfigUtil.getConfigRetriever(vertx)
        .rxGetConfig()
        .map(
            config -> {
              this.config = config;
              return config;
            })
        .flatMapCompletable(this::initializeClients)
        .doOnComplete(
            () ->
                SharedDataUtils.put(
                    vertx.getDelegate(),
                    TenantCache.getInstance(
                        Integer.parseInt(config.getString(TENANT_CONFIG_REFRESH_INTERVAL)))))
        .andThen(
            vertx.rxDeployVerticle(
                () ->
                    new RestVerticle(
                        new HttpServerOptions().setPort(Integer.parseInt(config.getString(PORT)))),
                new DeploymentOptions().setInstances(getNumOfCores())))
        .ignoreElement();
  }

  private Integer getNumOfCores() {
    return CpuCoreSensor.availableProcessors();
  }

  @Override
  public Completable rxStop() {
    this.redisClient.close();
    this.webClient.close();

    return this.mysqlClient.rxClose();
  }

  private Completable initializeClients(JsonObject config) {
    return initializeMysqlClient(config)
        .andThen(initializeRedisClient(config))
        .andThen(initializeWebClient(config));
  }

  private Completable initializeMysqlClient(JsonObject config) {
    this.mysqlClient = new MysqlClientImpl(this.vertx, config);

    SharedDataUtils.put(vertx.getDelegate(), this.mysqlClient);

    return mysqlClient.rxConnect();
  }

  private Completable initializeRedisClient(JsonObject config) {
    RedisOptions redisOptions =
        new RedisOptions()
            .setConnectionString(
                "redis://"
                    + config.getString(REDIS_HOST)
                    + ":"
                    + config.getString(REDIS_PORT)
                    + "/")
            .setType(RedisClientType.valueOf(config.getString(REDIS_TYPE)));
    this.redisClient = Redis.createClient(vertx, redisOptions);

    SharedDataUtils.put(vertx.getDelegate(), this.redisClient);

    return redisClient.rxConnect().ignoreElement();
  }

  private Completable initializeWebClient(JsonObject config) {
    WebClientOptions options =
        new WebClientOptions()
            .setConnectTimeout(Integer.parseInt(config.getString(HTTP_CONNECT_TIMEOUT)))
            .setIdleTimeoutUnit(TimeUnit.MILLISECONDS)
            .setReadIdleTimeout(Integer.parseInt(config.getString(HTTP_READ_TIMEOUT)))
            .setWriteIdleTimeout(Integer.parseInt(config.getString(HTTP_WRITE_TIMEOUT)));
    this.webClient = WebClient.create(vertx, options);

    SharedDataUtils.put(vertx.getDelegate(), this.webClient);

    return Completable.complete();
  }
}

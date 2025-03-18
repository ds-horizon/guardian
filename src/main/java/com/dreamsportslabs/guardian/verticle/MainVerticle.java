package com.dreamsportslabs.guardian.verticle;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.client.impl.MysqlClientImpl;
import com.dreamsportslabs.guardian.config.application.Config;
import com.dreamsportslabs.guardian.config.application.RedisConfig;
import com.dreamsportslabs.guardian.registry.Registry;
import com.dreamsportslabs.guardian.utils.ConfigUtil;
import com.dreamsportslabs.guardian.utils.SharedDataUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.redis.client.RedisOptions;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.ext.web.client.WebClient;
import io.vertx.rxjava3.redis.client.Redis;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainVerticle extends AbstractVerticle {
  private Redis redisClient;
  private WebClient webClient;
  private MysqlClient mysqlClient;

  protected final ObjectMapper objectMapper =
      JsonMapper.builder()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
          .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
          .serializationInclusion(JsonInclude.Include.NON_NULL)
          .build();

  @Override
  public Completable rxStart() {
    SharedDataUtils.put(vertx.getDelegate(), new Registry());
    // Todo: relook application config schema (CONFIG)
    return ConfigUtil.getConfigRetriever(vertx, "application.conf")
        .rxGetConfig()
        .map(json -> objectMapper.readValue(json.encode(), Config.class))
        .map(
            config -> {
              SharedDataUtils.put(vertx.getDelegate(), config);
              return config;
            })
        .flatMapCompletable(this::initializeClients)
        .andThen(
            Completable.fromAction(
                () -> SharedDataUtils.put(vertx.getDelegate(), TenantCache.getInstance())))
        .andThen(
            vertx.rxDeployVerticle(
                // Todo: read port from config (CONFIG)
                () -> new RestVerticle(new HttpServerOptions().setPort(8080)),
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

  private Completable initializeClients(Config config) {
    return initializeMysqlClient(JsonObject.mapFrom(config.getMySQLConfig()))
        .andThen(initializeRedisClient(config.getRedisConfig()))
        .andThen(initializeWebClient(JsonObject.mapFrom(config.getWebClientConfig())));
  }

  private Completable initializeMysqlClient(JsonObject config) {
    this.mysqlClient = new MysqlClientImpl(this.vertx);
    SharedDataUtils.put(vertx.getDelegate(), this.mysqlClient);
    return mysqlClient.rxConnect(config);
  }

  private Completable initializeRedisClient(RedisConfig redisConfig) {
    JsonObject redisJson = new JsonObject();
    redisJson.put(
        "connectionString", "redis://" + redisConfig.getHost() + ":" + redisConfig.getPort() + "/");
    redisJson.put("type", redisConfig.getType());
    this.redisClient = Redis.createClient(vertx, new RedisOptions(redisJson));
    SharedDataUtils.put(vertx.getDelegate(), this.redisClient);
    return redisClient.rxConnect().ignoreElement();
  }

  private Completable initializeWebClient(JsonObject config) {
    this.webClient = WebClient.create(vertx, new WebClientOptions(config));
    SharedDataUtils.put(vertx.getDelegate(), this.webClient);
    return Completable.complete();
  }
}

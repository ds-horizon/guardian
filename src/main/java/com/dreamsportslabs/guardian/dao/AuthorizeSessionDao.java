package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.CACHE_KEY_AUTH_SESSION;
import static com.dreamsportslabs.guardian.constant.Constants.EXPIRY_OPTION_REDIS;
import static com.dreamsportslabs.guardian.constant.Constants.KEEP_TTL;

import com.dreamsportslabs.guardian.dao.model.AuthorizeSessionModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.redis.client.Command;
import io.vertx.rxjava3.redis.client.Redis;
import io.vertx.rxjava3.redis.client.Request;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class AuthorizeSessionDao {
  private final Redis redisClient;
  private final ObjectMapper objectMapper;

  private static final int DEFAULT_TTL = 600;

  @SneakyThrows
  public Completable saveAuthorizeSession(
      String challenge, AuthorizeSessionModel model, String tenantId, Integer ttl) {
    String cacheKey = getCacheKey(challenge, tenantId);
    String value = model.toString();
    val redisClient = this.redisClient;

    return this.redisClient
        .rxSend(Request.cmd(Command.TTL).arg(cacheKey))
        .filter(response -> response.toInteger() == -2)
        .flatMap(
            response ->
                redisClient.rxSend(
                    Request.cmd(Command.SET)
                        .arg(cacheKey)
                        .arg(value)
                        .arg(EXPIRY_OPTION_REDIS)
                        .arg(ttl != null ? ttl : DEFAULT_TTL)))
        .switchIfEmpty(
            redisClient.rxSend(Request.cmd(Command.SET).arg(cacheKey).arg(value).arg(KEEP_TTL)))
        .doOnSuccess(
            resp ->
                log.info(
                    "Saving AuthorizeSession response key: " + cacheKey + " : " + resp.toString()))
        .ignoreElement();
  }

  public Single<AuthorizeSessionModel> getAuthorizeSession(String challenge, String tenantId) {
    String cacheKey = getCacheKey(challenge, tenantId);

    return redisClient
        .rxSend(Request.cmd(Command.GET).arg(cacheKey))
        .switchIfEmpty(Maybe.empty())
        .map(response -> objectMapper.readValue(response.toString(), AuthorizeSessionModel.class))
        .toSingle();
  }

  public Completable deleteAuthorizeSession(String challenge, String tenantId) {
    String cacheKey = getCacheKey(challenge, tenantId);

    return redisClient
        .rxSend(Request.cmd(Command.DEL).arg(cacheKey))
        .doOnSuccess(
            response ->
                log.info(
                    "Deleted AuthorizeSession with key: {} for tenant: {}", cacheKey, tenantId))
        .ignoreElement();
  }

  private String getCacheKey(String challenge, String tenantId) {
    return CACHE_KEY_AUTH_SESSION + "_" + tenantId + "_" + challenge;
  }
}

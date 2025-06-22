package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.CACHE_KEY_CONSENT_SESSION;
import static com.dreamsportslabs.guardian.constant.Constants.EXPIRY_OPTION_REDIS;

import com.dreamsportslabs.guardian.dao.model.ConsentSessionModel;
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

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ConsentSessionDao {
  private final Redis redisClient;
  private final ObjectMapper objectMapper;


  private static final int DEFAULT_TTL = 600;

  @SneakyThrows
  public Completable saveConsentSession(
      ConsentSessionModel model, String tenantId, Integer ttl) {
    String cacheKey = getCacheKey(model.getConsentChallenge(), tenantId);
    String value = model.toString();

    return redisClient
        .rxSend(
            Request.cmd(Command.SET)
                .arg(cacheKey)
                .arg(value)
                .arg(EXPIRY_OPTION_REDIS)
                .arg(ttl != null ? ttl : DEFAULT_TTL))
        .doOnSuccess(
            response ->
                log.info("Saved ConsentSession with key: {} for tenant: {}", cacheKey, tenantId))
        .ignoreElement();
  }

  public Single<ConsentSessionModel> getConsentSession(String consentChallenge, String tenantId) {
    String cacheKey = getCacheKey(consentChallenge, tenantId);

    return redisClient
        .rxSend(Request.cmd(Command.GET).arg(cacheKey))
        .switchIfEmpty(Maybe.empty())
        .map(response -> objectMapper.readValue(response.toString(), ConsentSessionModel.class))
        .toSingle();
  }

  public Completable deleteConsentSession(String consentChallenge, String tenantId) {
    String cacheKey = getCacheKey(consentChallenge, tenantId);

    return redisClient
        .rxSend(Request.cmd(Command.DEL).arg(cacheKey))
        .doOnSuccess(
            response ->
                log.info(
                    "Deleted ConsentSession with key: {} for tenant: {}", cacheKey, tenantId))
        .ignoreElement();
  }

  private String getCacheKey(String consentChallenge, String tenantId) {
    return CACHE_KEY_CONSENT_SESSION + "_" + tenantId + "_" + consentChallenge;
  }
} 
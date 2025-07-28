package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.EXPIRY_OPTION_REDIS;

import com.dreamsportslabs.guardian.dao.model.OidcCodeModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.rxjava3.redis.client.Command;
import io.vertx.rxjava3.redis.client.Redis;
import io.vertx.rxjava3.redis.client.Request;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class OidcCodeDao {
  private final Redis redisClient;
  private final ObjectMapper objectMapper;

  private static final String CACHE_KEY_OIDC_CODE = "OIDC_CODE";

  @SneakyThrows
  public Completable saveOidcCode(String code, OidcCodeModel model, String tenantId, Integer ttl) {
    String cacheKey = getCacheKey(code, tenantId);
    String value = objectMapper.writeValueAsString(model);

    return redisClient
        .rxSend(Request.cmd(Command.SET).arg(cacheKey).arg(value).arg(EXPIRY_OPTION_REDIS).arg(ttl))
        .doOnSuccess(
            response -> log.info("Saved OidcCode with key: {} for tenant: {}", cacheKey, tenantId))
        .ignoreElement();
  }

  public Maybe<OidcCodeModel> getOidcCode(String code, String tenantId) {
    String cacheKey = getCacheKey(code, tenantId);

    return redisClient
        .rxSend(Request.cmd(Command.GET).arg(cacheKey))
        .switchIfEmpty(Maybe.empty())
        .map(response -> objectMapper.readValue(response.toString(), OidcCodeModel.class));
  }

  public Completable deleteOidcCode(String code, String tenantId) {
    String cacheKey = getCacheKey(code, tenantId);

    return redisClient
        .rxSend(Request.cmd(Command.DEL).arg(cacheKey))
        .doOnSuccess(
            response ->
                log.info("Deleted OidcCode with key: {} for tenant: {}", cacheKey, tenantId))
        .ignoreElement();
  }

  private String getCacheKey(String code, String tenantId) {
    return CACHE_KEY_OIDC_CODE + "_" + tenantId + "_" + code;
  }
}

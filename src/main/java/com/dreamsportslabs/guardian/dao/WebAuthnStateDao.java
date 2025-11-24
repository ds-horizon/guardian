package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.EXPIRE_AT_REDIS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.dao.model.WebAuthnStateModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
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
public class WebAuthnStateDao {
  private final Redis redisClient;
  private final ObjectMapper objectMapper;
  private static final String CACHE_KEY_WEBAUTHN_STATE = "WEBAUTHN_STATE";

  public Maybe<WebAuthnStateModel> getWebAuthnState(String state, String tenantId) {
    return redisClient
        .rxSend(Request.cmd(Command.GET).arg(getCacheKey(tenantId, state)))
        .map(response -> objectMapper.readValue(response.toString(), WebAuthnStateModel.class));
  }

  /**
   * Atomically consumes (gets and deletes) a WebAuthn state to prevent race conditions. This
   * ensures that a state can only be used once, even under concurrent requests.
   */
  @SneakyThrows
  public Maybe<WebAuthnStateModel> consumeState(String state, String tenantId) {
    String cacheKey = getCacheKey(tenantId, state);
    // Use Lua script for atomic GET and DEL operation
    String luaScript =
        "local val = redis.call('GET', KEYS[1]); "
            + "if val then redis.call('DEL', KEYS[1]); return val; else return nil; end";
    return redisClient
        .rxSend(Request.cmd(Command.EVAL).arg(luaScript).arg("1").arg(cacheKey))
        .filter(
            response ->
                response != null
                    && response.toString() != null
                    && !response.toString().isEmpty()
                    && !response.toString().equals("nil"))
        .map(response -> objectMapper.readValue(response.toString(), WebAuthnStateModel.class));
  }

  @SneakyThrows
  public Single<WebAuthnStateModel> setWebAuthnState(WebAuthnStateModel model, String tenantId) {
    return redisClient
        .rxSend(
            Request.cmd(Command.SET)
                .arg(getCacheKey(tenantId, model.getState()))
                .arg(objectMapper.writeValueAsString(model))
                .arg(EXPIRE_AT_REDIS)
                .arg(model.getExpiry()))
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)))
        .map(response -> model)
        .toSingle();
  }

  public void deleteWebAuthnState(String state, String tenantId) {
    redisClient.rxSend(Request.cmd(Command.DEL).arg(getCacheKey(tenantId, state))).subscribe();
  }

  private String getCacheKey(String tenantId, String state) {
    return CACHE_KEY_WEBAUTHN_STATE + "_" + tenantId + "_" + state;
  }
}

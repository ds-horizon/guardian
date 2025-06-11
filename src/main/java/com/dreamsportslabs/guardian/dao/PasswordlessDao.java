package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.CACHE_KEY_STATE;
import static com.dreamsportslabs.guardian.constant.Constants.EXPIRY_OPTION_REDIS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.dao.model.PasswordlessModel;
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
public class PasswordlessDao {
  private final Redis redisClient;
  private final ObjectMapper objectMapper;

  public Maybe<PasswordlessModel> getPasswordlessModel(String state, String tenantId) {
    return redisClient
        .rxSend(Request.cmd(Command.GET).arg(getCacheKey(tenantId, state)))
        .map(response -> objectMapper.readValue(response.toString(), PasswordlessModel.class));
  }

  @SneakyThrows
  public Single<PasswordlessModel> setPasswordlessModel(PasswordlessModel model, String tenantId) {
    return redisClient
        .rxSend(
            Request.cmd(Command.SET)
                .arg(getCacheKey(tenantId, model.getState()))
                .arg(objectMapper.writeValueAsString(model))
                .arg(EXPIRY_OPTION_REDIS)
                // Todo: arbitrary expiry to eventually expire, > allowed max otp validity
                .arg(3600))
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)))
        .map(response -> model)
        .toSingle();
  }

  public void deletePasswordlessModel(String state, String tenantId) {
    redisClient.rxSend(Request.cmd(Command.DEL).arg(getCacheKey(tenantId, state))).subscribe();
  }

  private String getCacheKey(String tenantId, String state) {
    return CACHE_KEY_STATE + "_" + tenantId + "_" + state;
  }
}

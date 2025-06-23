package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.CACHE_KEY_STATE;
import static com.dreamsportslabs.guardian.constant.Constants.EXPIRY_OPTION_REDIS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.dao.model.OtpGenerateModel;
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
public class ContactVerifyDao {
  private final Redis redisClient;
  private final ObjectMapper objectMapper;

  public Maybe<OtpGenerateModel> getOtpGenerateModel(String tenantId, String state) {
    String cacheKey = getCacheKeyForOtp(tenantId, state);
    return redisClient
        .rxSend(Request.cmd(Command.GET).arg(cacheKey))
        .map(response -> objectMapper.readValue(response.toString(), OtpGenerateModel.class));
  }

  @SneakyThrows
  public Single<OtpGenerateModel> setOtpGenerateModel(
      OtpGenerateModel model, String tenantId, String state) {
    String cacheKey = getCacheKeyForOtp(tenantId, state);
    return redisClient
        .rxSend(
            Request.cmd(Command.SET)
                .arg(cacheKey)
                .arg(objectMapper.writeValueAsString(model))
                .arg(EXPIRY_OPTION_REDIS)
                .arg(model.getExpiry()))
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)))
        .map(response -> model)
        .toSingle();
  }

  public void deleteOtpGenerateModel(String tenantId, String state) {
    String cacheKey = getCacheKeyForOtp(tenantId, state);
    redisClient.rxSend(Request.cmd(Command.DEL).arg(cacheKey)).subscribe();
  }

  public String getCacheKeyForOtp(String tenantId, String state) {
    return CACHE_KEY_STATE + "_otp_only_" + tenantId + "_" + state;
  }
}

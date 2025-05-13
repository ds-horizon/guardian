package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.CACHE_KEY_CODE;
import static com.dreamsportslabs.guardian.constant.Constants.EXPIRY_OPTION_REDIS;

import com.dreamsportslabs.guardian.dao.model.CodeModel;
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
public class CodeDao {
  private final Redis redisClient;
  private final ObjectMapper objectMapper;

  @SneakyThrows
  public Completable saveCode(CodeModel model, String tenantId) {
    return redisClient
        .rxSend(
            Request.cmd(Command.SET)
                .arg(getCacheKey(model.getCode(), tenantId))
                .arg(objectMapper.writeValueAsString(model))
                .arg(EXPIRY_OPTION_REDIS)
                .arg(model.getExpiry()))
        .ignoreElement();
  }

  public Maybe<CodeModel> getCode(String code, String tenantId) {
    return redisClient
        .rxSend(Request.cmd(Command.GET).arg(getCacheKey(code, tenantId)))
        .map(response -> objectMapper.readValue(response.toString(), CodeModel.class));
  }

  public Completable deleteCode(String code, String tenantId) {
    return redisClient
        .rxSend(Request.cmd(Command.DEL).arg(getCacheKey(code, tenantId)))
        .ignoreElement();
  }

  private String getCacheKey(String code, String tenantId) {
    return CACHE_KEY_CODE + "_" + tenantId + "_" + code;
  }
}

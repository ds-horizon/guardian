package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.NEG_INF;
import static com.dreamsportslabs.guardian.constant.Constants.REDIS_OPTION_BYSCORE;
import static com.dreamsportslabs.guardian.constant.Constants.REVOCATIONS_KEY_APPLICATION_ID_INDEX;
import static com.dreamsportslabs.guardian.constant.Constants.REVOCATIONS_KEY_SCORE_END_INDEX;
import static com.dreamsportslabs.guardian.constant.Constants.REVOCATIONS_KEY_SCORE_START_INDEX;
import static com.dreamsportslabs.guardian.constant.Constants.REVOCATIONS_KEY_SEPARATOR;
import static com.dreamsportslabs.guardian.constant.Constants.REVOCATIONS_REDIS_KEY_PREFIX;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.redis.client.Command;
import io.vertx.rxjava3.redis.client.Redis;
import io.vertx.rxjava3.redis.client.Request;
import io.vertx.rxjava3.redis.client.Response;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class RevocationDao {
  final Redis redisClient;

  public Single<List<String>> getRevocations(String key) {
    return redisClient
        .rxSend(getRedisRequestToGetRevocations(key))
        .switchIfEmpty(Single.error(INTERNAL_SERVER_ERROR.getException()))
        .map(this::mapRedisResponseToList)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public void addExpiredRefreshTokensInSortedSet(
      long currentTimeStamp, List<String> refreshTokens, String tenantId) {
    Request req = Request.cmd(Command.ZADD).arg(getRevocationsCacheKey(tenantId));

    for (String sessionId : refreshTokens) {
      req.arg(currentTimeStamp);
      req.arg(sessionId);
    }
    redisClient.rxSend(req).subscribe();
  }

  public void removeExpiredRefreshTokensFromSortedSet(
      long currentTimeStamp, long accessTokenExpiry, String tenantId) {
    long expiryTimeStamp = currentTimeStamp - accessTokenExpiry;
    redisClient
        .rxSend(
            Request.cmd(Command.ZREMRANGEBYSCORE)
                .arg(getRevocationsCacheKey(tenantId))
                .arg(NEG_INF)
                .arg(expiryTimeStamp))
        .subscribe();
  }

  private List<String> mapRedisResponseToList(Response resp) {
    List<String> revocations = new ArrayList<>();
    resp.forEach(item -> revocations.add(item.toString()));
    return revocations;
  }

  private Request getRedisRequestToGetRevocations(String key) {
    String[] revocationKeyParts = key.split(REVOCATIONS_KEY_SEPARATOR);
    String tenantId = revocationKeyParts[REVOCATIONS_KEY_APPLICATION_ID_INDEX];
    String fromEpoch = revocationKeyParts[REVOCATIONS_KEY_SCORE_START_INDEX];
    String toEpoch = revocationKeyParts[REVOCATIONS_KEY_SCORE_END_INDEX];
    return Request.cmd(Command.ZRANGE)
        .arg(getRevocationsCacheKey(tenantId))
        .arg(fromEpoch)
        .arg(toEpoch)
        .arg(REDIS_OPTION_BYSCORE);
  }

  private String getRevocationsCacheKey(String applicationId) {
    return REVOCATIONS_REDIS_KEY_PREFIX + REVOCATIONS_KEY_SEPARATOR + applicationId;
  }
}

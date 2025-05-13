package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.RefreshTokenSql.GET_ALL_REFRESH_TOKENS_FOR_USER;
import static com.dreamsportslabs.guardian.dao.query.RefreshTokenSql.INVALIDATE_ALL_REFRESH_TOKENS_FOR_USER;
import static com.dreamsportslabs.guardian.dao.query.RefreshTokenSql.INVALIDATE_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.dao.query.RefreshTokenSql.SAVE_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.dao.query.RefreshTokenSql.VALIDATE_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.RefreshTokenModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__({@Inject}))
public class RefreshTokenDao {
  private final MysqlClient mysqlClient;

  public Completable saveRefreshToken(RefreshTokenModel refreshTokenModel) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(SAVE_REFRESH_TOKEN)
        .rxExecute(
            Tuple.tuple(
                Arrays.asList(
                    refreshTokenModel.getTenantId(),
                    refreshTokenModel.getUserId(),
                    refreshTokenModel.getRefreshToken(),
                    refreshTokenModel.getRefreshTokenExp(),
                    refreshTokenModel.getSource(),
                    refreshTokenModel.getDeviceName(),
                    refreshTokenModel.getLocation(),
                    refreshTokenModel.getIp())))
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)))
        .ignoreElement();
  }

  public Maybe<String> getRefreshToken(String refreshToken, String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(VALIDATE_REFRESH_TOKEN)
        .rxExecute(Tuple.of(tenantId, refreshToken))
        .filter(rowSet -> rowSet.size() > 0)
        .map(rowSet -> JsonUtils.rowSetToList(rowSet, String.class).get(0));
  }

  public Completable invalidateRefreshToken(String refreshToken, String tenantId) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(INVALIDATE_REFRESH_TOKEN)
        .rxExecute(Tuple.of(tenantId, refreshToken))
        .ignoreElement();
  }

  public Completable invalidateAllRefreshTokensForUser(String userId, String tenantId) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(INVALIDATE_ALL_REFRESH_TOKENS_FOR_USER)
        .rxExecute(Tuple.of(tenantId, userId))
        .ignoreElement();
  }

  public Single<List<String>> getRefreshTokens(String userId, String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_ALL_REFRESH_TOKENS_FOR_USER)
        .rxExecute(Tuple.of(tenantId, userId))
        .map(rows -> JsonUtils.rowSetToList(rows, String.class));
  }
}

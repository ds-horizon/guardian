package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.OidcTokenQuery.GET_OIDC_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.dao.query.OidcTokenQuery.REVOKE_OIDC_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.dao.query.OidcTokenQuery.SAVE_OIDC_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.OidcRefreshTokenModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class OidcRefreshTokenDao {
  private final MysqlClient mysqlClient;

  public Completable saveOidcRefreshToken(OidcRefreshTokenModel refreshTokenModel) {
    Tuple params = Tuple.tuple();
    params.addString(refreshTokenModel.getTenantId());
    params.addString(refreshTokenModel.getClientId());
    params.addString(refreshTokenModel.getUserId());
    params.addString(refreshTokenModel.getRefreshToken());
    params.addLong(refreshTokenModel.getRefreshTokenExp());
    params.addJsonArray(new JsonArray(refreshTokenModel.getScope()));
    params.addString(refreshTokenModel.getDeviceName());
    params.addString(refreshTokenModel.getIp());

    return mysqlClient
        .getWriterPool()
        .preparedQuery(SAVE_OIDC_REFRESH_TOKEN)
        .rxExecute(params)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException()))
        .ignoreElement();
  }

  public Maybe<OidcRefreshTokenModel> getOidcRefreshToken(
      String tenantId, String clientId, String refreshToken) {
    Tuple params = Tuple.tuple();
    params.addString(tenantId);
    params.addString(clientId);
    params.addString(refreshToken);
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_OIDC_REFRESH_TOKEN)
        .rxExecute(params)
        .onErrorResumeNext(
            err -> {
              log.error("Failed to get OIDC refresh token", err);
              return Single.error(INTERNAL_SERVER_ERROR.getException());
            })
        .filter(result -> result.size() > 0)
        .switchIfEmpty(Maybe.empty())
        .map(result -> JsonUtils.rowSetToList(result, OidcRefreshTokenModel.class).get(0));
  }

  public Single<Boolean> revokeOidcRefreshToken(
      String tenantId, String clientId, String refreshToken) {
    Tuple params = Tuple.of(tenantId, clientId, refreshToken);
    return mysqlClient
        .getWriterPool()
        .preparedQuery(REVOKE_OIDC_REFRESH_TOKEN)
        .rxExecute(params)
        .onErrorResumeNext(
            err -> {
              log.error("Failed to revoke OIDC refresh token", err);
              return Single.error(
                  INTERNAL_SERVER_ERROR.getJsonCustomException(
                      "Failed to revoke OIDC refresh token"));
            })
        .filter(result -> result.rowCount() > 0)
        .map(__ -> true)
        .switchIfEmpty(Single.just(false));
  }
}

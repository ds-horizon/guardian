package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.OidcTokenQuery.GET_ACTIVE_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.dao.query.OidcTokenQuery.GET_OIDC_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.dao.query.OidcTokenQuery.REVOKE_OIDC_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.dao.query.OidcTokenQuery.SAVE_OIDC_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.dao.query.SsoTokenQuery.SAVE_SSO_TOKEN;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.RefreshTokenModel;
import com.dreamsportslabs.guardian.dao.model.SsoTokenModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class RefreshTokenDao {
  private final MysqlClient mysqlClient;

  public Completable saveRefreshToken(RefreshTokenModel refreshTokenModel) {
    return saveRefreshToken(refreshTokenModel, null);
  }

  public Completable saveRefreshToken(
      RefreshTokenModel refreshTokenModel, SsoTokenModel ssoTokenModel) {
    Tuple refreshTokenParams = Tuple.tuple();
    refreshTokenParams.addString(refreshTokenModel.getTenantId());
    refreshTokenParams.addString(refreshTokenModel.getClientId());
    refreshTokenParams.addString(refreshTokenModel.getUserId());
    refreshTokenParams.addString(refreshTokenModel.getRefreshToken());
    refreshTokenParams.addLong(refreshTokenModel.getRefreshTokenExp());
    refreshTokenParams.addJsonArray(new JsonArray(refreshTokenModel.getScope()));
    refreshTokenParams.addString(refreshTokenModel.getDeviceName());
    refreshTokenParams.addString(refreshTokenModel.getIp());
    refreshTokenParams.addString(refreshTokenModel.getSource());
    refreshTokenParams.addString(refreshTokenModel.getLocation());
    refreshTokenParams.addJsonArray(new JsonArray(refreshTokenModel.getAuthMethod()));

    Tuple ssoTokenParams = Tuple.tuple();
    if (ssoTokenModel != null) {
      ssoTokenParams.addString(ssoTokenModel.getTenantId());
      ssoTokenParams.addString(ssoTokenModel.getClientIdIssuedTo());
      ssoTokenParams.addString(ssoTokenModel.getUserId());
      ssoTokenParams.addString(ssoTokenModel.getRefreshToken());
      ssoTokenParams.addString(ssoTokenModel.getSsoToken());
      ssoTokenParams.addLong(ssoTokenModel.getExpiry());
      ssoTokenParams.addJsonArray(new JsonArray(ssoTokenModel.getAuthMethods()));
    }

    return mysqlClient
        .getWriterPool()
        .rxWithTransaction(
            client ->
                client
                    .preparedQuery(SAVE_OIDC_REFRESH_TOKEN)
                    .rxExecute(refreshTokenParams)
                    .filter(rows -> ssoTokenModel != null)
                    .flatMapSingle(
                        resp -> client.preparedQuery(SAVE_SSO_TOKEN).rxExecute(ssoTokenParams)))
        .doOnSuccess(v -> log.info("Token saved successfully"))
        .doOnError(err -> log.error("Error saving refreshToken or ssoToken"))
        .ignoreElement();
  }

  public Maybe<RefreshTokenModel> getRefreshToken(String tenantId, String refreshToken) {
    Tuple params = Tuple.tuple();
    params.addString(tenantId);
    params.addString(refreshToken);
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_ACTIVE_REFRESH_TOKEN)
        .rxExecute(params)
        .onErrorResumeNext(
            err -> {
              log.error("Failed to get refresh token", err);
              return Single.error(INTERNAL_SERVER_ERROR.getException());
            })
        .filter(result -> result.size() > 0)
        .switchIfEmpty(Maybe.empty())
        .map(result -> JsonUtils.rowSetToList(result, RefreshTokenModel.class).get(0));
  }

  public Maybe<RefreshTokenModel> getRefreshToken(
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
        .map(result -> JsonUtils.rowSetToList(result, RefreshTokenModel.class).get(0));
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

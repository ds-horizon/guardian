package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.OidcTokenQuery.GET_OIDC_REFRESH_TOKEN;
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
    params.addJsonArray(JsonArray.of(refreshTokenModel.getScope()));
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
      String tenantId, String refreshToken, String clientId) {
    Tuple params = Tuple.tuple();
    params.addString(tenantId);
    params.addString(refreshToken);
    params.addString(clientId);
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_OIDC_REFRESH_TOKEN)
        .rxExecute(params)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException()))
        .filter(result -> result.size() > 0)
        .switchIfEmpty(Maybe.empty())
        .map(result -> JsonUtils.rowSetToList(result, OidcRefreshTokenModel.class).get(0));
  }
}

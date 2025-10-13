package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.SsoTokenQuery.GET_ACTIVE_SSO_TOKEN;
import static com.dreamsportslabs.guardian.dao.query.SsoTokenQuery.GET_SSO_TOKEN_FROM_REFRESH_TOKEN;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.SsoTokenModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class SsoTokenDao {
  private final MysqlClient mysqlClient;

  public Maybe<SsoTokenModel> getSsoToken(String ssoToken, String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_ACTIVE_SSO_TOKEN)
        .rxExecute(Tuple.of(tenantId, ssoToken))
        .filter(rows -> rows.size() > 0)
        .map(rowSet -> JsonUtils.rowSetToList(rowSet, SsoTokenModel.class).get(0));
  }

  public Maybe<SsoTokenModel> getSsoTokenFromRefreshToken(String refreshToken, String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_SSO_TOKEN_FROM_REFRESH_TOKEN)
        .rxExecute(Tuple.of(tenantId, refreshToken))
        .filter(rows -> rows.size() > 0)
        .map(rowSet -> JsonUtils.rowSetToList(rowSet, SsoTokenModel.class).get(0));
  }
}

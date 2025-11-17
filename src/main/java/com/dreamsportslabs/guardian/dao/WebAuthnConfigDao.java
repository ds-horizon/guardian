package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.WebAuthnConfigSql.GET_WEBAUTHN_CONFIG;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.WebAuthnConfigModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class WebAuthnConfigDao {
  private final MysqlClient mysqlClient;

  public Maybe<WebAuthnConfigModel> getWebAuthnConfig(String tenantId, String clientId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_WEBAUTHN_CONFIG)
        .rxExecute(Tuple.of(tenantId, clientId))
        .filter(result -> result.size() > 0)
        .switchIfEmpty(Maybe.empty())
        .map(result -> JsonUtils.rowSetToList(result, WebAuthnConfigModel.class).get(0));
  }
}

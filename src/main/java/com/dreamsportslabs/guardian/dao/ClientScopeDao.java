package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.ClientScopeQuery.DELETE_CLIENT_SCOPE;
import static com.dreamsportslabs.guardian.dao.query.ClientScopeQuery.DELETE_CLIENT_SCOPES_BY_CLIENT;
import static com.dreamsportslabs.guardian.dao.query.ClientScopeQuery.INSERT_CLIENT_SCOPE;
import static com.dreamsportslabs.guardian.dao.query.ClientScopeQuery.SELECT_CLIENT_SCOPES;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.ClientScopeModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.mysqlclient.MySQLException;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ClientScopeDao {
  private final MysqlClient mysqlClient;

  public Single<ClientScopeModel> createClientScope(ClientScopeModel clientScope) {
    Tuple params =
        Tuple.of(clientScope.getTenantId(), clientScope.getScope(), clientScope.getClientId());

    return mysqlClient
        .getWriterPool()
        .preparedQuery(INSERT_CLIENT_SCOPE)
        .rxExecute(params)
        .map(result -> clientScope)
        .onErrorResumeNext(
            err -> {
              if (!(err instanceof MySQLException mySQLException)) {
                return Single.error(INTERNAL_SERVER_ERROR.getException(err));
              }
              if (mySQLException.getErrorCode() == 1062) {
                return Single.error(
                    INVALID_REQUEST.getCustomException(
                        clientScope.getScope() + " already exists for client"));
              }
              return Single.error(INTERNAL_SERVER_ERROR.getException(err));
            });
  }

  public Single<List<ClientScopeModel>> getClientScopes(String clientId, String tenantId) {
    Tuple params = Tuple.of(clientId, tenantId);

    return mysqlClient
        .getWriterPool()
        .preparedQuery(SELECT_CLIENT_SCOPES)
        .rxExecute(params)
        .map(result -> JsonUtils.rowSetToList(result, ClientScopeModel.class))
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteClientScope(String tenantId, String clientId, String scope) {
    Tuple params = Tuple.of(tenantId, clientId, scope);

    return mysqlClient
        .getWriterPool()
        .preparedQuery(DELETE_CLIENT_SCOPE)
        .rxExecute(params)
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteClientScopesByClient(String clientId, String tenantId) {
    Tuple params = Tuple.of(clientId, tenantId);

    return mysqlClient
        .getWriterPool()
        .preparedQuery(DELETE_CLIENT_SCOPES_BY_CLIENT)
        .rxExecute(params)
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }
}

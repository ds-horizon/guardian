package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.ClientScopeQuery.DELETE_CLIENT_SCOPE;
import static com.dreamsportslabs.guardian.dao.query.ClientScopeQuery.GET_CLIENT_SCOPES;
import static com.dreamsportslabs.guardian.dao.query.ClientScopeQuery.INSERT_CLIENT_SCOPE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.SCOPE_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.ClientScopeModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ClientScopeDao {
  private final MysqlClient mysqlClient;

  public Completable createClientScope(List<ClientScopeModel> clientScope) {
    List<Tuple> tuples =
        clientScope.stream()
            .map(scope -> Tuple.of(scope.getTenantId(), scope.getClientId(), scope.getScope()))
            .toList();
    log.info("Creating client scope with {} clients", tuples.size());
    return mysqlClient
        .getWriterPool()
        .preparedQuery(INSERT_CLIENT_SCOPE)
        .rxExecuteBatch(tuples)
        .ignoreElement()
        .onErrorResumeNext(
            err ->
                Completable.error(
                    SCOPE_ALREADY_EXISTS.getCustomException("Scope already exists for client")));
  }

  public Single<List<ClientScopeModel>> getClientScopes(String clientId, String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_CLIENT_SCOPES)
        .rxExecute(Tuple.of(tenantId, clientId))
        .map(result -> JsonUtils.rowSetToList(result, ClientScopeModel.class))
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable deleteClientScope(String tenantId, String clientId, String scope) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(DELETE_CLIENT_SCOPE)
        .rxExecute(Tuple.of(tenantId, clientId, scope))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }
}

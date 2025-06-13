package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.OidcConfigQuery.*;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.ScopeModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.mysqlclient.MySQLClient;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ScopeDao {
  private final MysqlClient mysqlClient;

  public Single<List<ScopeModel>> getScopesByName(String tenantId, String scope) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_SCOPES_BY_NAME)
        .execute(Tuple.of(tenantId, scope))
        .map(rowSet -> JsonUtils.rowSetToList(rowSet, ScopeModel.class));
  }

  public Single<List<ScopeModel>> getAllScopes(String tenantId, int offset, int limit) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_ALL_SCOPES)
        .execute(Tuple.of(tenantId, limit, offset))
        .map(rowSet -> JsonUtils.rowSetToList(rowSet, ScopeModel.class));
  }

  public Single<ScopeModel> saveScopes(ScopeModel model) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(CREATE_SCOPE)
        .execute(
            Tuple.of(
                model.getTenantId(),
                model.getScope(),
                model.getDisplayName(),
                model.getDescription(),
                new JsonArray(model.getClaims())))
        .map(rows -> String.valueOf(rows.property(MySQLClient.LAST_INSERTED_ID)))
        .map(
            rowId -> {
              model.setId(Integer.parseInt(rowId));
              return model;
            });
  }

  public Single<Boolean> deleteScope(String tenantId, String scope) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(DELETE_SCOPE)
        .execute(Tuple.of(tenantId, scope))
        .map(result -> result.rowCount() > 0);
  }
}

package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.ScopeQuery.DELETE_SCOPE;
import static com.dreamsportslabs.guardian.dao.query.ScopeQuery.GET_SCOPES_BY_NAMES_TEMPLATE;
import static com.dreamsportslabs.guardian.dao.query.ScopeQuery.GET_SCOPES_PAGINATED;
import static com.dreamsportslabs.guardian.dao.query.ScopeQuery.GET_SCOPE_BY_NAME;
import static com.dreamsportslabs.guardian.dao.query.ScopeQuery.SAVE_SCOPE;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.ScopeModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.mysqlclient.MySQLClient;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ScopeDao {
  private final MysqlClient mysqlClient;

  public Single<List<ScopeModel>> getScope(String tenantId, String name) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_SCOPE_BY_NAME)
        .execute(Tuple.of(tenantId, name))
        .map(rowSet -> JsonUtils.rowSetToList(rowSet, ScopeModel.class));
  }

  public Single<List<ScopeModel>> getScopesByNames(String tenantId, List<String> names) {
    String placeholders = String.join(",", Collections.nCopies(names.size(), "?"));
    String query = String.format(GET_SCOPES_BY_NAMES_TEMPLATE, placeholders);

    Tuple tuple = Tuple.tuple();
    tuple.addString(tenantId);
    for (String name : names) {
      tuple.addString(name);
    }

    return mysqlClient
        .getReaderPool()
        .preparedQuery(query)
        .execute(tuple)
        .map(rowSet -> JsonUtils.rowSetToList(rowSet, ScopeModel.class));
  }

  public Single<List<ScopeModel>> getScopesWithPagination(String tenantId, int offset, int limit) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_SCOPES_PAGINATED)
        .execute(Tuple.of(tenantId, limit, offset))
        .map(rowSet -> JsonUtils.rowSetToList(rowSet, ScopeModel.class));
  }

  public Single<ScopeModel> saveScopes(ScopeModel model) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(SAVE_SCOPE)
        .execute(
            Tuple.wrap(
                Arrays.asList(
                    model.getTenantId(),
                    model.getName(),
                    model.getDisplayName(),
                    model.getDescription(),
                    new JsonArray(model.getClaims() == null ? List.of() : model.getClaims()),
                    model.getIsOidc(),
                    model.getIconUrl())))
        .map(rows -> String.valueOf(rows.property(MySQLClient.LAST_INSERTED_ID)))
        .map(rowId -> model);
  }

  public Single<Boolean> deleteScope(String tenantId, String name) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(DELETE_SCOPE)
        .execute(Tuple.of(tenantId, name))
        .map(result -> result.rowCount() > 0);
  }
}

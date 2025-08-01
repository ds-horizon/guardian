package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.UserFlowBlockSql.GET_ACTIVE_FLOW_BLOCKS_BY_USER_IDENTIFIER;
import static com.dreamsportslabs.guardian.dao.query.UserFlowBlockSql.GET_FLOW_BLOCK_REASON_BATCH;
import static com.dreamsportslabs.guardian.dao.query.UserFlowBlockSql.UNBLOCK_USER_FLOW;
import static com.dreamsportslabs.guardian.dao.query.UserFlowBlockSql.UPSERT_USER_FLOW_BLOCK;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.constant.BlockFlow;
import com.dreamsportslabs.guardian.dao.model.UserFlowBlockModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__({@Inject}))
public class UserFlowBlockDao {

  private final MysqlClient mysqlClient;

  public Completable blockFlows(List<UserFlowBlockModel> models) {
    List<Tuple> batchParams =
        models.stream()
            .map(
                model ->
                    Tuple.tuple()
                        .addValue(model.getTenantId())
                        .addValue(model.getUserIdentifier())
                        .addValue(model.getFlowName())
                        .addValue(model.getReason())
                        .addValue(model.getUnblockedAt())
                        .addValue(model.isActive()))
            .collect(Collectors.toList());

    return mysqlClient
        .getWriterPool()
        .preparedQuery(UPSERT_USER_FLOW_BLOCK)
        .rxExecuteBatch(batchParams)
        .ignoreElement()
        .onErrorResumeNext(
            err -> {
              log.error("Failed to block user flows", err);
              return Completable.error(INTERNAL_SERVER_ERROR.getException(err));
            });
  }

  public Completable unblockFlows(String tenantId, String userIdentifier, List<String> flowNames) {
    List<Tuple> batchParams =
        flowNames.stream()
            .map(flowName -> Tuple.of(tenantId, userIdentifier, flowName))
            .collect(Collectors.toList());

    return mysqlClient
        .getWriterPool()
        .preparedQuery(UNBLOCK_USER_FLOW)
        .rxExecuteBatch(batchParams)
        .ignoreElement();
  }

  public Single<List<String>> getActiveFlowBlocksByUser(String tenantId, String userIdentifier) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_ACTIVE_FLOW_BLOCKS_BY_USER_IDENTIFIER)
        .rxExecute(Tuple.of(tenantId, userIdentifier))
        .map(rowSet -> JsonUtils.rowSetToList(rowSet, String.class));
  }

  public Single<List<UserFlowBlockModel>> checkFlowBlockedWithReasonBatch(
      String tenantId, List<String> userIdentifiers, BlockFlow flowName) {

    String placeholders = String.join(",", userIdentifiers.stream().map(c -> "?").toList());
    String query = String.format(GET_FLOW_BLOCK_REASON_BATCH, placeholders);

    Tuple params = Tuple.tuple().addValue(tenantId).addValue(flowName.getFlowName());
    for (String user : userIdentifiers) {
      params.addValue(user);
    }

    return mysqlClient
        .getReaderPool()
        .preparedQuery(query)
        .rxExecute(params)
        .map(rowSet -> JsonUtils.rowSetToList(rowSet, UserFlowBlockModel.class));
  }
}

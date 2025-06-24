package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.ContactFlowBlockSql.CHECK_FLOW_BLOCKED;
import static com.dreamsportslabs.guardian.dao.query.ContactFlowBlockSql.GET_ACTIVE_FLOW_BLOCKS_BY_CONTACT;
import static com.dreamsportslabs.guardian.dao.query.ContactFlowBlockSql.UNBLOCK_CONTACT_FLOW;
import static com.dreamsportslabs.guardian.dao.query.ContactFlowBlockSql.UPSERT_CONTACT_FLOW_BLOCK;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.ContactFlowBlockModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__({@Inject}))
public class ContactFlowBlockDao {
    private final MysqlClient mysqlClient;

    public Completable blockFlows(ContactFlowBlockModel model) {
        return mysqlClient
                .getWriterPool()
                .preparedQuery(UPSERT_CONTACT_FLOW_BLOCK)
                .rxExecute(
                        Tuple.tuple(
                                List.of(
                                        model.getTenantId(),
                                        model.getContact(),
                                        model.getFlowName(),
                                        model.getReason(),
                                        model.getOperator(),
                                        model.getUnblockedAt(),
                                        model.isActive())))
                .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)))
                .ignoreElement();
    }

    public Completable unblockFlows(String tenantId, String contact, String flowName) {
        return mysqlClient
                .getWriterPool()
                .preparedQuery(UNBLOCK_CONTACT_FLOW)
                .rxExecute(Tuple.of(tenantId, contact, flowName))
                .ignoreElement();
    }

    public Single<List<ContactFlowBlockModel>> getActiveFlowBlocksByContact(
            String tenantId, String contact) {
        return mysqlClient
                .getReaderPool()
                .preparedQuery(GET_ACTIVE_FLOW_BLOCKS_BY_CONTACT)
                .rxExecute(Tuple.of(tenantId, contact))
                .map(rows -> JsonUtils.rowSetToList(rows, ContactFlowBlockModel.class));
    }

    public Single<Boolean> isFlowBlocked(String tenantId, String contact, String flowName) {
        return mysqlClient
                .getReaderPool()
                .preparedQuery(CHECK_FLOW_BLOCKED)
                .rxExecute(Tuple.of(tenantId, contact, flowName))
                .map(rows -> rows.iterator().next().getInteger("count") > 0);
    }
} 
package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.ContactBlockSql.CHECK_API_BLOCKED;
import static com.dreamsportslabs.guardian.dao.query.ContactBlockSql.GET_ACTIVE_BLOCKS_BY_CONTACT;
import static com.dreamsportslabs.guardian.dao.query.ContactBlockSql.UNBLOCK_CONTACT_API;
import static com.dreamsportslabs.guardian.dao.query.ContactBlockSql.UPSERT_CONTACT_BLOCK;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.ContactApiBlockModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__({@Inject}))
public class ContactBlockDao {
  private final MysqlClient mysqlClient;

  public Completable blockApis(ContactApiBlockModel model) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(UPSERT_CONTACT_BLOCK)
        .rxExecute(
            Tuple.tuple(
                List.of(
                    model.getTenantId(),
                    model.getContact(),
                    model.getApiPath(),
                    model.getReason(),
                    model.getOperator(),
                    model.getUnblockedAt(),
                    model.isActive())))
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)))
        .ignoreElement();
  }

  public Completable unblockApis(String tenantId, String contact, String apiPath) {
    String query = String.format(UNBLOCK_CONTACT_API);

    List<Object> params = new ArrayList<>();
    params.add(tenantId);
    params.add(contact);
    params.add(apiPath);

    return mysqlClient
        .getWriterPool()
        .preparedQuery(query)
        .rxExecute(Tuple.tuple(params))
        .ignoreElement();
  }

  public Single<List<ContactApiBlockModel>> getActiveBlocksByContact(
      String tenantId, String contact) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_ACTIVE_BLOCKS_BY_CONTACT)
        .rxExecute(Tuple.of(tenantId, contact))
        .map(rows -> JsonUtils.rowSetToList(rows, ContactApiBlockModel.class));
  }

  public Single<Boolean> isApiBlocked(String tenantId, String contact, String apiPath) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(CHECK_API_BLOCKED)
        .rxExecute(Tuple.of(tenantId, contact, apiPath))
        .map(rows -> rows.iterator().next().getInteger("count") > 0);
  }
}

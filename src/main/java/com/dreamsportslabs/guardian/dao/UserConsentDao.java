package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.UserConsentQuery.DELETE_USER_CONSENTS;
import static com.dreamsportslabs.guardian.dao.query.UserConsentQuery.GET_USER_CONSENTS;
import static com.dreamsportslabs.guardian.dao.query.UserConsentQuery.INSERT_CONSENT;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.UserConsentModel;
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
public class UserConsentDao {
  private final MysqlClient mysqlClient;

  public Completable insertConsent(String tenantId, String clientId, String userId, String scope) {
    Tuple params = Tuple.of(tenantId, clientId, userId, scope);

    return mysqlClient
        .getWriterPool()
        .preparedQuery(INSERT_CONSENT)
        .rxExecute(params)
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<List<UserConsentModel>> getUserConsents(
      String tenantId, String clientId, String userId) {
    Tuple params = Tuple.of(tenantId, clientId, userId);

    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_USER_CONSENTS)
        .rxExecute(params)
        .map(result -> JsonUtils.rowSetToList(result, UserConsentModel.class))
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable deleteUserConsents(String tenantId, String clientId, String userId) {
    Tuple params = Tuple.of(tenantId, clientId, userId);

    return mysqlClient
        .getWriterPool()
        .preparedQuery(DELETE_USER_CONSENTS)
        .rxExecute(params)
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }
}

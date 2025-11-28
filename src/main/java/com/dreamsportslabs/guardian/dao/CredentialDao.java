package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.CredentialSql.GET_ACTIVE_CREDENTIALS_BY_USER_AND_CLIENT;
import static com.dreamsportslabs.guardian.dao.query.CredentialSql.GET_CREDENTIAL_BY_ID;
import static com.dreamsportslabs.guardian.dao.query.CredentialSql.MARK_FIRST_USE_COMPLETE;
import static com.dreamsportslabs.guardian.dao.query.CredentialSql.REVOKE_CREDENTIAL;
import static com.dreamsportslabs.guardian.dao.query.CredentialSql.SAVE_CREDENTIAL;
import static com.dreamsportslabs.guardian.dao.query.CredentialSql.UPDATE_SIGN_COUNT;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.CredentialModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class CredentialDao {
  private final MysqlClient mysqlClient;

  public Single<List<CredentialModel>> getActiveCredentialsByUserAndClient(
      String tenantId, String clientId, String userId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_ACTIVE_CREDENTIALS_BY_USER_AND_CLIENT)
        .rxExecute(Tuple.of(tenantId, clientId, userId))
        .map(rows -> JsonUtils.rowSetToList(rows, CredentialModel.class));
  }

  public Maybe<CredentialModel> getCredentialById(
      String tenantId, String clientId, String userId, String credentialId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_CREDENTIAL_BY_ID)
        .rxExecute(Tuple.of(tenantId, clientId, userId, credentialId))
        .filter(result -> result.size() > 0)
        .switchIfEmpty(Maybe.empty())
        .map(result -> JsonUtils.rowSetToList(result, CredentialModel.class).get(0));
  }

  public Completable saveCredential(CredentialModel credential) {
    Tuple params = Tuple.tuple();
    params.addString(credential.getTenantId());
    params.addString(credential.getClientId());
    params.addString(credential.getUserId());
    params.addString(credential.getCredentialId());
    params.addString(credential.getPublicKey());
    params.addString(credential.getBindingType());
    params.addInteger(credential.getAlg());
    params.addLong(credential.getSignCount() != null ? credential.getSignCount() : 0L);
    params.addString(credential.getAaguid());
    params.addBoolean(
        credential.getFirstUseComplete() != null ? credential.getFirstUseComplete() : false);

    return mysqlClient
        .getWriterPool()
        .preparedQuery(SAVE_CREDENTIAL)
        .rxExecute(params)
        .doOnSuccess(v -> log.info("Credential saved successfully"))
        .doOnError(err -> log.error("Error saving credential", err))
        .ignoreElement();
  }

  public Completable revokeCredential(
      String tenantId, String clientId, String userId, String credentialId) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(REVOKE_CREDENTIAL)
        .rxExecute(Tuple.of(tenantId, clientId, userId, credentialId))
        .doOnSuccess(v -> log.info("Credential revoked successfully"))
        .doOnError(err -> log.error("Error revoking credential", err))
        .ignoreElement();
  }

  public Completable updateSignCount(
      String tenantId, String clientId, String userId, String credentialId, Long signCount) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(UPDATE_SIGN_COUNT)
        .rxExecute(Tuple.of(signCount, tenantId, clientId, userId, credentialId))
        .doOnSuccess(v -> log.info("Sign count updated successfully"))
        .doOnError(err -> log.error("Error updating sign count", err))
        .ignoreElement();
  }

  public Completable markFirstUseComplete(
      String tenantId, String clientId, String userId, String credentialId) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(MARK_FIRST_USE_COMPLETE)
        .rxExecute(Tuple.of(tenantId, clientId, userId, credentialId))
        .doOnSuccess(v -> log.info("First use marked as complete"))
        .doOnError(err -> log.error("Error marking first use complete", err))
        .ignoreElement();
  }
}

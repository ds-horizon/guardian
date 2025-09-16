package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.ClientQuery.CREATE_CLIENT;
import static com.dreamsportslabs.guardian.dao.query.ClientQuery.DELETE_CLIENT;
import static com.dreamsportslabs.guardian.dao.query.ClientQuery.GET_CLIENT;
import static com.dreamsportslabs.guardian.dao.query.ClientQuery.GET_CLIENTS;
import static com.dreamsportslabs.guardian.dao.query.ClientQuery.GET_DEFAULT_CLIENT;
import static com.dreamsportslabs.guardian.dao.query.ClientQuery.UPDATE_CLIENT;
import static com.dreamsportslabs.guardian.dao.query.ClientQuery.UPDATE_CLIENT_SECRET;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.CLIENT_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.utils.JsonUtils.serializeToJsonString;
import static com.dreamsportslabs.guardian.utils.SqlUtils.prepareUpdateQuery;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.ClientModel;
import com.dreamsportslabs.guardian.dto.request.UpdateClientRequestDto;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.mysqlclient.MySQLException;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ClientDao {
  private final MysqlClient mysqlClient;
  private final ObjectMapper objectMapper;

  public Single<ClientModel> createClient(ClientModel client) {
    Tuple params = Tuple.tuple();
    params
        .addString(client.getTenantId())
        .addString(client.getClientId())
        .addString(client.getClientName())
        .addString(client.getClientSecret())
        .addString(client.getClientUri())
        .addString(serializeToJsonString(client.getContacts(), objectMapper))
        .addString(serializeToJsonString(client.getGrantTypes(), objectMapper))
        .addString(client.getLogoUri())
        .addString(client.getPolicyUri())
        .addString(serializeToJsonString(client.getRedirectUris(), objectMapper))
        .addString(serializeToJsonString(client.getResponseTypes(), objectMapper))
        .addBoolean(client.getSkipConsent());
    return mysqlClient
        .getWriterPool()
        .preparedQuery(CREATE_CLIENT)
        .rxExecute(params)
        .map(result -> client)
        .onErrorResumeNext(
            err -> {
              if (err instanceof MySQLException mySQLException
                  && mySQLException.getErrorCode() == 1062) {
                return Single.error(
                    CLIENT_ALREADY_EXISTS.getCustomException(
                        client.getClientName() + " already exists"));
              }

              return Single.error(INTERNAL_SERVER_ERROR.getException(err));
            });
  }

  public Maybe<ClientModel> getClient(String clientId, String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_CLIENT)
        .rxExecute(Tuple.of(tenantId, clientId))
        .flatMapMaybe(
            result -> {
              if (result.size() == 0) {
                return Maybe.empty();
              }
              return Maybe.just(JsonUtils.rowSetToList(result, ClientModel.class).get(0));
            })
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Maybe<ClientModel> getDefaultClient(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_DEFAULT_CLIENT)
        .rxExecute(Tuple.of(tenantId))
        .flatMapMaybe(
            result -> {
              if (result.size() == 0) {
                return Maybe.empty();
              }
              return Maybe.just(JsonUtils.rowSetToList(result, ClientModel.class).get(0));
            })
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<List<ClientModel>> getClients(String tenantId, int limit, int offset) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_CLIENTS)
        .rxExecute(Tuple.of(tenantId, limit, offset))
        .map(result -> JsonUtils.rowSetToList(result, ClientModel.class))
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateClient(
      UpdateClientRequestDto updateRequest, String clientId, String tenantId) {

    Pair<String, Tuple> queryAndTuple = prepareUpdateQuery(updateRequest);
    Tuple tuple = queryAndTuple.getRight().addString(tenantId).addString(clientId);
    String query = UPDATE_CLIENT.replace("<<insert_attributes>>", queryAndTuple.getLeft());

    return mysqlClient
        .getWriterPool()
        .preparedQuery(query)
        .rxExecute(tuple)
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateClientSecret(String newSecret, String clientId, String tenantId) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(UPDATE_CLIENT_SECRET)
        .rxExecute(Tuple.of(newSecret, tenantId, clientId))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteClient(String clientId, String tenantId) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(DELETE_CLIENT)
        .rxExecute(Tuple.of(tenantId, clientId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }
}

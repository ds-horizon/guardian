package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.ClientQuery.DELETE_CLIENT;
import static com.dreamsportslabs.guardian.dao.query.ClientQuery.INSERT_CLIENT;
import static com.dreamsportslabs.guardian.dao.query.ClientQuery.SELECT_CLIENTS_BY_TENANT;
import static com.dreamsportslabs.guardian.dao.query.ClientQuery.SELECT_CLIENT_BY_ID;
import static com.dreamsportslabs.guardian.dao.query.ClientQuery.UPDATE_CLIENT;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.CLIENT_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.ClientModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.mysqlclient.MySQLException;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ClientDao {
  private final MysqlClient mysqlClient;
  private final ObjectMapper objectMapper;

  public Single<ClientModel> createClient(ClientModel client) {
    try {
      Tuple params = Tuple.tuple();
      params
          .addString(client.getTenantId())
          .addString(client.getClientId())
          .addString(client.getClientName())
          .addString(client.getClientSecret())
          .addString(client.getClientUri())
          .addString(objectMapper.writeValueAsString(client.getContacts()))
          .addString(objectMapper.writeValueAsString(client.getGrantTypes()))
          .addString(client.getLogoUri())
          .addString(client.getPolicyUri())
          .addString(objectMapper.writeValueAsString(client.getRedirectUris()))
          .addString(objectMapper.writeValueAsString(client.getResponseTypes()))
          .addBoolean(client.getSkipConsent());
      return mysqlClient
          .getWriterPool()
          .preparedQuery(INSERT_CLIENT)
          .rxExecute(params)
          .map(result -> client)
          .onErrorResumeNext(
              err -> {
                if (!(err instanceof MySQLException mySQLException)) {
                  return Single.error(INTERNAL_SERVER_ERROR.getException(err));
                }
                if (mySQLException.getErrorCode() == 1062) {
                  return Single.error(
                      CLIENT_ALREADY_EXISTS.getCustomException(
                          client.getClientName() + " already exists"));
                }
                return Single.error(INTERNAL_SERVER_ERROR.getException(err));
              });
    } catch (JsonProcessingException e) {
      return Single.error(INTERNAL_SERVER_ERROR.getException(e));
    }
  }

  public Maybe<ClientModel> getClientById(String clientId, String tenantId) {
    Tuple params = Tuple.of(clientId, tenantId);

    return mysqlClient
        .getReaderPool()
        .preparedQuery(SELECT_CLIENT_BY_ID)
        .rxExecute(params)
        .flatMapMaybe(
            result -> {
              if (result.size() == 0) {
                return Maybe.empty();
              }
              return Maybe.just(JsonUtils.rowSetToList(result, ClientModel.class).get(0));
            })
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<List<ClientModel>> getClientsByTenant(String tenantId, int limit, int offset) {
    Tuple params = Tuple.of(tenantId, limit, offset);

    return mysqlClient
        .getReaderPool()
        .preparedQuery(SELECT_CLIENTS_BY_TENANT)
        .rxExecute(params)
        .map(result -> JsonUtils.rowSetToList(result, ClientModel.class))
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<ClientModel> updateClient(ClientModel client) {
    try {
      Tuple params = Tuple.tuple();
      params
          .addString(client.getClientName())
          .addString(client.getClientSecret())
          .addString(client.getClientUri())
          .addString(objectMapper.writeValueAsString(client.getContacts()))
          .addString(objectMapper.writeValueAsString(client.getGrantTypes()))
          .addString(client.getLogoUri())
          .addString(client.getPolicyUri())
          .addString(objectMapper.writeValueAsString(client.getRedirectUris()))
          .addString(objectMapper.writeValueAsString(client.getResponseTypes()))
          .addBoolean(client.getSkipConsent())
          .addString(client.getClientId())
          .addString(client.getTenantId());

      return mysqlClient
          .getWriterPool()
          .preparedQuery(UPDATE_CLIENT)
          .rxExecute(params)
          .map(result -> client)
          .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
    } catch (JsonProcessingException e) {
      return Single.error(INTERNAL_SERVER_ERROR.getException(e));
    }
  }

  public Single<Boolean> deleteClient(String clientId, String tenantId) {
    Tuple params = Tuple.of(clientId, tenantId);

    return mysqlClient
        .getWriterPool()
        .preparedQuery(DELETE_CLIENT)
        .rxExecute(params)
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }
}

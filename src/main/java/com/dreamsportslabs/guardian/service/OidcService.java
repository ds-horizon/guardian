<<<<<<< HEAD
package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.ClientQuery.CREATE_CLIENT;
import static com.dreamsportslabs.guardian.dao.query.ClientQuery.DELETE_CLIENT;
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
=======
package com.dreamsportslabs.guardian.service;

import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.dao.AuthorizeSessionDao;
import com.dreamsportslabs.guardian.dao.ClientScopeDao;
import com.dreamsportslabs.guardian.dao.model.AuthorizeSessionModel;
import com.dreamsportslabs.guardian.dao.model.ClientModel;
import com.dreamsportslabs.guardian.dao.model.ClientScopeModel;
import com.dreamsportslabs.guardian.dto.request.AuthorizeRequestDto;
import com.dreamsportslabs.guardian.dto.response.AuthorizeResponseDto;
import com.dreamsportslabs.guardian.exception.OidcErrorEnum;
import com.dreamsportslabs.guardian.registry.Registry;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
>>>>>>> e725673 (add authorize api)
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
<<<<<<< HEAD
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
          .preparedQuery(CREATE_CLIENT)
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
=======
public class OidcService {
  private final ClientService clientService;
  private final ClientScopeDao clientScopeDao;
  private final AuthorizeSessionDao authorizeSessionDao;
  private final Registry registry;

  public Single<AuthorizeResponseDto> authorize(
      AuthorizeRequestDto requestDto, MultivaluedMap<String, String> headers, String tenantId) {

    return clientService
        .getClient(requestDto.getClientId(), tenantId)
        .switchIfEmpty(
            Single.error(
                OidcErrorEnum.INVALID_REQUEST.getCustomException(
                    "Invalid client_id", requestDto.getState(), requestDto.getRedirectUri())))
        .flatMap(client -> validateClient(client, requestDto, tenantId))
        .flatMap(
            client -> {
              String loginChallenge = UUID.randomUUID().toString();

              return filterSupportedScopes(
                      requestDto.getClientId(), requestDto.getScope(), tenantId)
                  .map(
                      supportedScopes -> {
                        AuthorizeSessionModel sessionModel =
                            new AuthorizeSessionModel(requestDto, loginChallenge);
                        sessionModel.setScope(supportedScopes);
                        return sessionModel;
                      })
                  .flatMap(
                      sessionModel ->
                          authorizeSessionDao
                              .saveAuthorizeSession(sessionModel, tenantId, 600)
                              .andThen(
                                  Single.fromCallable(
                                      () -> {
                                        TenantConfig tenantConfig =
                                            registry.get(tenantId, TenantConfig.class);
                                        String loginPageUri =
                                            tenantConfig.getOidcConfig() != null
                                                ? tenantConfig.getOidcConfig().getLoginPageUri()
                                                : null;

                                        return new AuthorizeResponseDto(
                                            loginChallenge, requestDto.getState(), loginPageUri);
                                      })));
            });
  }

  private Single<ClientModel> validateClient(
      ClientModel client, AuthorizeRequestDto requestDto, String tenantId) {
    validateRedirectUri(client, requestDto);
    validateResponseType(client, requestDto);
    validateScope(requestDto);
    return Single.just(client);
  }

  private void validateRedirectUri(ClientModel client, AuthorizeRequestDto requestDto) {
    if (client.getRedirectUris() == null
        || !client.getRedirectUris().contains(requestDto.getRedirectUri())) {
      throw OidcErrorEnum.INVALID_REQUEST.getCustomException(
          "Invalid redirect_uri", requestDto.getState(), requestDto.getRedirectUri());
    }
  }

  private void validateResponseType(ClientModel client, AuthorizeRequestDto requestDto) {
    if (client.getResponseTypes() == null
        || !client.getResponseTypes().contains(requestDto.getResponseType())) {
      throw OidcErrorEnum.UNSUPPORTED_RESPONSE_TYPE.getCustomException(
          "Unsupported response_type", requestDto.getState(), requestDto.getRedirectUri());
    }
  }

  private void validateScope(AuthorizeRequestDto requestDto) {
    String[] scopes = requestDto.getScope().split(" ");
    boolean hasOpenid = false;

    for (String scope : scopes) {
      if ("openid".equals(scope.trim())) {
        hasOpenid = true;
        break;
      }
    }

    if (!hasOpenid) {
      throw OidcErrorEnum.INVALID_SCOPE.getCustomException(
          "scope must contain 'openid'", requestDto.getState(), requestDto.getRedirectUri());
    }
  }

  private Single<String> filterSupportedScopes(
      String clientId, String requestedScopes, String tenantId) {
    String[] requestedScopeArray = requestedScopes.split(" ");

    return clientScopeDao
        .getClientScopes(clientId, tenantId)
        .map(
            clientScopes -> {
              Set<String> allowedScopes =
                  clientScopes.stream().map(ClientScopeModel::getScope).collect(Collectors.toSet());

              StringBuilder supportedScopes = new StringBuilder();
              for (String scope : requestedScopeArray) {
                String trimmedScope = scope.trim();
                if (allowedScopes.contains(trimmedScope)) {
                  if (supportedScopes.length() > 0) {
                    supportedScopes.append(" ");
                  }
                  supportedScopes.append(trimmedScope);
                }
              }

              return supportedScopes.toString();
            });
  }
}
>>>>>>> e725673 (add authorize api)

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
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
                        sessionModel.setClient(client);
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

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
import java.util.Arrays;
import java.util.List;
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

  public Single<AuthorizeResponseDto> authorize(AuthorizeRequestDto requestDto, String tenantId) {
    return clientService
        .getClient(requestDto.getClientId(), tenantId)
        .onErrorResumeNext(
            __ ->
                Single.error(
                    OidcErrorEnum.INVALID_CLIENT.getJsonCustomException(
                        "Client authentication failed")))
        .map(client -> validateClient(client, requestDto, tenantId))
        .flatMap(
            client -> {
              String loginChallenge = UUID.randomUUID().toString();

              return clientScopeDao
                  .getClientScopes(requestDto.getClientId(), tenantId)
                  .map(clientScopes -> filterAllowedScopes(requestDto.getScope(), clientScopes))
                  .map(
                      allowedScopes ->
                          AuthorizeSessionModel.builder()
                              .responseType(requestDto.getResponseType())
                              .allowedScopes(allowedScopes)
                              .client(client)
                              .redirectUri(requestDto.getRedirectUri())
                              .state(requestDto.getState())
                              .nonce(requestDto.getNonce())
                              .codeChallenge(requestDto.getCodeChallenge())
                              .codeChallengeMethod(requestDto.getCodeChallengeMethod())
                              .prompt(requestDto.getPrompt())
                              .loginHint(requestDto.getLoginHint())
                              .build())
                  .flatMap(
                      sessionModel ->
                          saveSession(loginChallenge, sessionModel, requestDto, tenantId));
            });
  }

  private Single<AuthorizeResponseDto> saveSession(
      String loginChallenge,
      AuthorizeSessionModel sessionModel,
      AuthorizeRequestDto requestDto,
      String tenantId) {

    TenantConfig tenantConfig = registry.get(tenantId, TenantConfig.class);
    int authorizeTtl = tenantConfig.getOidcConfig().getAuthorizeTtl();
    String loginPageUri = tenantConfig.getOidcConfig().getLoginPageUri();

    return authorizeSessionDao
        .saveAuthorizeSession(loginChallenge, sessionModel, tenantId, authorizeTtl)
        .andThen(
            Single.fromCallable(
                () ->
                    AuthorizeResponseDto.builder()
                        .loginChallenge(loginChallenge)
                        .state(requestDto.getState())
                        .loginPageUri(loginPageUri)
                        .prompt(requestDto.getPrompt())
                        .loginHint(requestDto.getLoginHint())
                        .build()));
  }

  private ClientModel validateClient(
      ClientModel client, AuthorizeRequestDto requestDto, String tenantId) {
    validateRedirectUri(client, requestDto);
    validateResponseType(client, requestDto);
    validateScope(requestDto);
    return client;
  }

  private void validateRedirectUri(ClientModel client, AuthorizeRequestDto requestDto) {
    if (client.getRedirectUris() == null
        || !client.getRedirectUris().contains(requestDto.getRedirectUri())) {
      throw OidcErrorEnum.INVALID_REDIRECT_URI.getJsonCustomException("Redirect uri is invalid");
    }
  }

  private void validateResponseType(ClientModel client, AuthorizeRequestDto requestDto) {
    if (client.getResponseTypes() == null
        || !client.getResponseTypes().contains(requestDto.getResponseType())) {
      throw OidcErrorEnum.UNSUPPORTED_RESPONSE_TYPE.getRedirectCustomException(
          "Unsupported response_type", requestDto.getState(), requestDto.getRedirectUri());
    }
  }

  private void validateScope(AuthorizeRequestDto requestDto) {
    String[] scopes = requestDto.getScope().split("\\s+");
    for (String scope : scopes) {
      if ("openid".equals(scope.trim())) {
        return;
      }
    }
    throw OidcErrorEnum.INVALID_SCOPE.getRedirectCustomException(
        "scope must contain 'openid'", requestDto.getState(), requestDto.getRedirectUri());
  }

  private List<String> filterAllowedScopes(
      String requestedScopes, List<ClientScopeModel> clientScopes) {
    Set<String> clientAllowedScopes =
        clientScopes.stream().map(ClientScopeModel::getScope).collect(Collectors.toSet());

    return Arrays.stream(requestedScopes.split("\\s+"))
        .filter(scope -> !scope.isEmpty())
        .filter(clientAllowedScopes::contains)
        .collect(Collectors.toList());
  }
}

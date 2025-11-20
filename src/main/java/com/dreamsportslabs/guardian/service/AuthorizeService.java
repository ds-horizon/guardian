package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.SCOPE_OPENID;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OIDC_CONFIG_NOT_EXISTS;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_CLIENT;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_REDIRECT_URI;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_SCOPE;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.UNSUPPORTED_RESPONSE_TYPE;

import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.dao.AuthorizeSessionDao;
import com.dreamsportslabs.guardian.dao.ClientScopeDao;
import com.dreamsportslabs.guardian.dao.model.AuthorizeSessionModel;
import com.dreamsportslabs.guardian.dao.model.ClientModel;
import com.dreamsportslabs.guardian.dao.model.ClientScopeModel;
import com.dreamsportslabs.guardian.dto.request.AuthorizeRequestDto;
import com.dreamsportslabs.guardian.dto.response.AuthorizeResponseDto;
import com.dreamsportslabs.guardian.registry.Registry;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class AuthorizeService {
  private final ClientService clientService;
  private final ClientScopeDao clientScopeDao;
  private final AuthorizeSessionDao authorizeSessionDao;
  private final Registry registry;

  public Single<AuthorizeResponseDto> authorize(AuthorizeRequestDto requestDto, String tenantId) {
    return clientService
        .getClient(requestDto.getClientId(), tenantId)
        .onErrorResumeNext(__ -> Single.error(INVALID_CLIENT.getJsonException()))
        .map(client -> validateClient(client, requestDto, tenantId))
        .flatMap(
            client -> {
              String loginChallenge = UUID.randomUUID().toString();

              return clientScopeDao
                  .getClientScopes(requestDto.getClientId(), tenantId)
                  .map(clientScopes -> filterAllowedScopes(requestDto.getScope(), clientScopes))
                  .map(
                      allowedScopes ->
                          buildAuthorizeSessionModel(requestDto, client, allowedScopes))
                  .flatMap(
                      sessionModel -> {
                        TenantConfig tenantConfig = registry.get(tenantId, TenantConfig.class);
                        if (tenantConfig.getOidcConfig() == null) {
                          throw OIDC_CONFIG_NOT_EXISTS.getException();
                        }
                        int authorizeTtl = tenantConfig.getOidcConfig().getAuthorizeTtl();
                        String loginPageUri = tenantConfig.getOidcConfig().getLoginPageUri();
                        return saveSession(loginChallenge, sessionModel, tenantId, authorizeTtl)
                            .andThen(
                                Single.fromCallable(
                                    () ->
                                        buildAuthorizeResponseDto(
                                            requestDto, loginChallenge, loginPageUri)));
                      });
            });
  }

  private AuthorizeSessionModel buildAuthorizeSessionModel(
      AuthorizeRequestDto requestDto, ClientModel client, List<String> allowedScopes) {
    return AuthorizeSessionModel.builder()
        .responseType(requestDto.getOidcResponseType())
        .allowedScopes(allowedScopes)
        .client(client)
        .redirectUri(requestDto.getRedirectUri())
        .state(requestDto.getState())
        .nonce(requestDto.getNonce())
        .codeChallenge(requestDto.getCodeChallenge())
        .codeChallengeMethod(requestDto.getOidcCodeChallengeMethod())
        .prompt(requestDto.getOidcPrompt())
        .loginHint(requestDto.getLoginHint())
        .build();
  }

  private AuthorizeResponseDto buildAuthorizeResponseDto(
      AuthorizeRequestDto requestDto, String loginChallenge, String loginPageUri) {
    return AuthorizeResponseDto.builder()
        .loginChallenge(loginChallenge)
        .state(requestDto.getState())
        .loginPageUri(loginPageUri)
        .prompt(requestDto.getPrompt())
        .loginHint(requestDto.getLoginHint())
        .build();
  }

  private Completable saveSession(
      String loginChallenge,
      AuthorizeSessionModel sessionModel,
      String tenantId,
      int authorizeTtl) {

    return authorizeSessionDao.saveAuthorizeSession(
        loginChallenge, sessionModel, tenantId, authorizeTtl);
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
      throw INVALID_REDIRECT_URI.getJsonException();
    }
  }

  private void validateResponseType(ClientModel client, AuthorizeRequestDto requestDto) {
    if (client.getResponseTypes() == null
        || !client.getResponseTypes().contains(requestDto.getOidcResponseType())) {
      throw UNSUPPORTED_RESPONSE_TYPE.getRedirectException(
          requestDto.getState(), requestDto.getRedirectUri());
    }
  }

  private void validateScope(AuthorizeRequestDto requestDto) {
    String[] scopes = requestDto.getScope().split("\\s+");
    for (String scope : scopes) {
      if (SCOPE_OPENID.equals(scope.trim())) {
        return;
      }
    }
    throw INVALID_SCOPE.getRedirectCustomException(
        "scope must contain 'openid'", requestDto.getState(), requestDto.getRedirectUri());
  }

  private List<String> filterAllowedScopes(
      String requestedScopes, List<ClientScopeModel> clientScopes) {
    Set<String> clientAllowedScopes =
        clientScopes.stream().map(ClientScopeModel::getScope).collect(Collectors.toSet());

    Set<String> requestedScopesSet =
        Arrays.stream(requestedScopes.split("\\s+")).collect(Collectors.toSet());

    requestedScopesSet.retainAll(clientAllowedScopes);

    return new ArrayList<>(requestedScopesSet);
  }
}

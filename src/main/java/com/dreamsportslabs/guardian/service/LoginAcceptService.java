package com.dreamsportslabs.guardian.service;

import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.constant.ClientType;
import com.dreamsportslabs.guardian.dao.AuthorizeSessionDao;
import com.dreamsportslabs.guardian.dao.RefreshTokenDao;
import com.dreamsportslabs.guardian.dao.UserConsentDao;
import com.dreamsportslabs.guardian.dao.model.AuthorizeSessionModel;
import com.dreamsportslabs.guardian.dao.model.OidcCodeModel;
import com.dreamsportslabs.guardian.dao.model.UserConsentModel;
import com.dreamsportslabs.guardian.dto.request.LoginAcceptRequestDto;
import com.dreamsportslabs.guardian.dto.response.AuthCodeResponseDto;
import com.dreamsportslabs.guardian.dto.response.LoginAcceptResponseDto;
import com.dreamsportslabs.guardian.registry.Registry;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class LoginAcceptService {
  private final AuthorizeSessionDao authorizeSessionDao;
  private final OidcCodeService oidcCodeService;
  private final OidcTokenService oidcTokenService;
  private final UserConsentDao userConsentDao;
  private final RefreshTokenDao refreshTokenDao;
  private final Registry registry;

  public Single<Object> loginAccept(LoginAcceptRequestDto requestDto, String tenantId) {

    return oidcTokenService
        .validateRefreshToken(requestDto.getRefreshToken(), tenantId)
        .flatMap(
            userId ->
                authorizeSessionDao
                    .getAuthorizeSession(requestDto.getLoginChallenge(), tenantId)
                    .flatMap(
                        authorizeSession -> {
                          authorizeSession.setUserId(userId);

                          if (authorizeSession
                              .getClient()
                              .getClientType()
                              .equals(ClientType.FIRST_PARTY.getValue())) {
                            return handleSkipConsentFlow(authorizeSession, tenantId);
                          }

                          return getUserConsentedScopes(
                                  authorizeSession.getClient().getClientId(), userId, tenantId)
                              .flatMap(
                                  consentedScopes -> {
                                    List<String> commonScopesList =
                                        getCommonScopes(
                                            authorizeSession.getAllowedScopes(), consentedScopes);
                                    if (commonScopesList.size()
                                        == authorizeSession.getAllowedScopes().size()) {
                                      return handleSkipConsentFlow(authorizeSession, tenantId);
                                    } else {
                                      authorizeSession.setConsentedScopes(commonScopesList);
                                      return handleConsentRequiredFlow(authorizeSession, tenantId);
                                    }
                                  });
                        })
                    .map(
                        response -> {
                          deleteLoginChallengeAsync(requestDto.getLoginChallenge(), tenantId);
                          return response;
                        }));
  }

  private Single<AuthCodeResponseDto> handleSkipConsentFlow(
      AuthorizeSessionModel authorizeSession, String tenantId) {
    authorizeSession.setConsentedScopes(authorizeSession.getAllowedScopes());
    String code = RandomStringUtils.randomAlphanumeric(32);
    OidcCodeModel oidcCodeModel = new OidcCodeModel(authorizeSession);
    return oidcCodeService
        .saveOidcCode(code, oidcCodeModel, tenantId)
        .toSingleDefault(code)
        .map(
            oidcCode ->
                new AuthCodeResponseDto(
                    authorizeSession.getRedirectUri(), authorizeSession.getState(), oidcCode));
  }

  private Single<LoginAcceptResponseDto> handleConsentRequiredFlow(
      AuthorizeSessionModel authorizeSession, String tenantId) {
    String consentChallenge = UUID.randomUUID().toString();
    TenantConfig tenantConfig = registry.get(tenantId, TenantConfig.class);

    return authorizeSessionDao
        .saveAuthorizeSession(
            consentChallenge,
            authorizeSession,
            tenantId,
            tenantConfig.getOidcConfig().getAuthorizeTtl())
        .toSingleDefault(
            new LoginAcceptResponseDto(
                tenantConfig.getOidcConfig().getConsentPageUri(),
                consentChallenge,
                authorizeSession.getState()));
  }

  private Single<Set<String>> getUserConsentedScopes(
      String clientId, String userId, String tenantId) {
    return userConsentDao
        .getUserConsents(tenantId, clientId, userId)
        .map(
            consents ->
                consents.stream().map(UserConsentModel::getScope).collect(Collectors.toSet()));
  }

  private List<String> getCommonScopes(List<String> requiredScopes, Set<String> consentedScopes) {
    Set<String> allowedScopesSet = new HashSet<>(requiredScopes);
    Set<String> intersection = new HashSet<>(consentedScopes);
    intersection.retainAll(allowedScopesSet);
    return new ArrayList<>(intersection);
  }

  private void deleteLoginChallengeAsync(String loginChallenge, String tenantId) {
    if (loginChallenge != null) {
      authorizeSessionDao
          .deleteAuthorizeSession(loginChallenge, tenantId)
          .subscribe(
              () -> log.info("Successfully deleted login challenge: {}", loginChallenge),
              error ->
                  log.warn(
                      "Failed to delete login challenge: {}, error: {}",
                      loginChallenge,
                      error.getMessage()));
    }
  }
}

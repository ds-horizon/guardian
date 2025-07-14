package com.dreamsportslabs.guardian.service;

import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.dao.AuthorizeSessionDao;
import com.dreamsportslabs.guardian.dao.OidcCodeDao;
import com.dreamsportslabs.guardian.dao.RefreshTokenDao;
import com.dreamsportslabs.guardian.dao.UserConsentDao;
import com.dreamsportslabs.guardian.dao.model.AuthorizeSessionModel;
import com.dreamsportslabs.guardian.dao.model.OidcCodeModel;
import com.dreamsportslabs.guardian.dao.model.UserConsentModel;
import com.dreamsportslabs.guardian.dto.request.LoginAcceptRequestDto;
import com.dreamsportslabs.guardian.dto.response.AuthCodeResponseDto;
import com.dreamsportslabs.guardian.dto.response.LoginAcceptResponseDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.registry.Registry;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.Maybe;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response.ResponseBuilder;
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
  private final OidcCodeDao oidcCodeDao;
  private final UserConsentDao userConsentDao;
  private final RefreshTokenDao refreshTokenDao;
  private final Registry registry;

  public Single<ResponseBuilder> loginAccept(
      LoginAcceptRequestDto requestDto, MultivaluedMap<String, String> headers, String tenantId) {

    return validateRefreshToken(requestDto.getRefreshToken(), tenantId)
        .flatMap(
            userId ->
                validateLoginChallenge(requestDto.getLoginChallenge(), tenantId)
                    .flatMap(
                        authorizeSession -> {
                          authorizeSession.setUserId(userId);
                          if (Boolean.TRUE.equals(authorizeSession.getClient().getSkipConsent())) {
                            return handleSkipConsentFlow(
                                authorizeSession, requestDto.getLoginChallenge(), tenantId);
                          }

                          return getUserConsentedScopes(
                                  authorizeSession.getClient().getClientId(), userId, tenantId)
                              .flatMap(
                                  consentedScopes -> {
                                    if (hasAllRequiredConsents(
                                        authorizeSession.getAllowedScopes(), consentedScopes)) {
                                      return handleSkipConsentFlow(
                                          authorizeSession,
                                          requestDto.getLoginChallenge(),
                                          tenantId);
                                    } else {
                                      return handleConsentRequiredFlow(authorizeSession, tenantId);
                                    }
                                  });
                        }));
  }

  private Single<ResponseBuilder> handleSkipConsentFlow(
      AuthorizeSessionModel authorizeSession, String loginChallenge, String tenantId) {
    authorizeSession.setConsentedScopes(authorizeSession.getAllowedScopes());
    String code = RandomStringUtils.randomAlphanumeric(32);
    OidcCodeModel codeSession = new OidcCodeModel(authorizeSession);
    return oidcCodeDao
        .saveCodeSession(code, codeSession, tenantId, 600)
        .toSingleDefault(code)
        .map(
            oidcCode ->
                new AuthCodeResponseDto(
                        authorizeSession.getRedirectUri(), authorizeSession.getState(), oidcCode)
                    .toResponse())
        .doOnSuccess(response -> deleteLoginChallengeAsync(loginChallenge, tenantId));
  }

  private Single<ResponseBuilder> handleConsentRequiredFlow(
      AuthorizeSessionModel authorizeSession, String tenantId) {
    String consentChallenge = UUID.randomUUID().toString();
    TenantConfig tenantConfig = registry.get(tenantId, TenantConfig.class);
    String consentPageUri =
        tenantConfig.getOidcConfig() != null
            ? tenantConfig.getOidcConfig().getConsentPageUri()
            : null;
    return authorizeSessionDao
        .saveAuthorizeSession(consentChallenge, authorizeSession, tenantId, 600)
        .toSingleDefault(new LoginAcceptResponseDto(consentPageUri, consentChallenge).toResponse());
  }

  private Single<String> validateRefreshToken(String refreshToken, String tenantId) {
    return refreshTokenDao
        .getRefreshToken(refreshToken, tenantId)
        .switchIfEmpty(
            Single.error(ErrorEnum.UNAUTHORIZED.getCustomException("Invalid refresh token")));
  }

  // Validate login challenge and return session
  private Single<AuthorizeSessionModel> validateLoginChallenge(
      String loginChallenge, String tenantId) {
    return authorizeSessionDao
        .getAuthorizeSession(loginChallenge, tenantId)
        .onErrorResumeNext(
            err ->
                Single.error(
                    ErrorEnum.INVALID_REQUEST.getCustomException("Invalid login challenge")));
  }

  private Single<Set<String>> getUserConsentedScopes(
      String clientId, String userId, String tenantId) {
    return userConsentDao
        .getUserConsents(tenantId, clientId, userId)
        .map(
            consents ->
                consents.stream().map(UserConsentModel::getScope).collect(Collectors.toSet()));
  }

  private boolean hasAllRequiredConsents(List<String> requiredScopes, Set<String> consentedScopes) {
    for (String scope : requiredScopes) {
      if (!consentedScopes.contains(scope.trim())) {
        return false;
      }
    }
    return true;
  }

  private void deleteLoginChallengeAsync(String loginChallenge, String tenantId) {
    if (loginChallenge != null) {
      authorizeSessionDao
          .deleteAuthorizeSession(loginChallenge, tenantId)
          .subscribe(
              () -> log.debug("Successfully deleted login challenge: {}", loginChallenge),
              error ->
                  log.warn(
                      "Failed to delete login challenge: {}, error: {}",
                      loginChallenge,
                      error.getMessage()));
    }
  }
}

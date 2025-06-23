package com.dreamsportslabs.guardian.service;

import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.dao.AuthorizeSessionDao;
import com.dreamsportslabs.guardian.dao.CodeSessionDao;
import com.dreamsportslabs.guardian.dao.RefreshTokenDao;
import com.dreamsportslabs.guardian.dao.UserConsentDao;
import com.dreamsportslabs.guardian.dao.model.AuthorizeSessionModel;
import com.dreamsportslabs.guardian.dao.model.CodeSessionModel;
import com.dreamsportslabs.guardian.dao.model.UserConsentModel;
import com.dreamsportslabs.guardian.dto.request.LoginAcceptRequestDto;
import com.dreamsportslabs.guardian.dto.response.AuthCodeResponseDto;
import com.dreamsportslabs.guardian.dto.response.LoginAcceptResponseDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.registry.Registry;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
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
  private final CodeSessionDao codeSessionDao;
  private final UserConsentDao userConsentDao;
  private final RefreshTokenDao refreshTokenDao;
  private final Registry registry;

  public Single<Response> loginAccept(
      LoginAcceptRequestDto requestDto, MultivaluedMap<String, String> headers, String tenantId) {

    return validateRefreshToken(requestDto.getRefreshToken(), tenantId)
        .flatMap(
            userId ->
                validateLoginChallengeAndProcess(requestDto.getLoginChallenge(), userId, tenantId));
  }

  private Single<String> validateRefreshToken(String refreshToken, String tenantId) {
    return refreshTokenDao
        .getRefreshToken(refreshToken, tenantId)
        .switchIfEmpty(
            Single.error(ErrorEnum.UNAUTHORIZED.getCustomException("Invalid refresh token")));
  }

  private Single<Response> validateLoginChallengeAndProcess(
      String loginChallenge, String userId, String tenantId) {
    return authorizeSessionDao
        .getAuthorizeSession(loginChallenge, tenantId)
        .onErrorResumeNext(
            err ->
                Single.error(
                    ErrorEnum.INVALID_REQUEST.getCustomException("Invalid login challenge")))
        .flatMap(session -> processConsentDecision(session, userId, tenantId, loginChallenge));
  }

  private Single<Response> processConsentDecision(
      AuthorizeSessionModel session, String userId, String tenantId, String loginChallenge) {
    if (Boolean.TRUE.equals(session.getClient().getSkipConsent())) {
      return handleSkipConsent(session, userId, tenantId, loginChallenge);
    } else {
      return checkExistingConsent(session, userId, tenantId, loginChallenge);
    }
  }

  private Single<Response> handleSkipConsent(
      AuthorizeSessionModel session, String userId, String tenantId, String loginChallenge) {
    session.setConsentedScopes(session.getAllowedScopes());
    return generateAndStoreAuthorizationCode(session, userId, tenantId)
        .map(code -> createCodeResponse(session, code))
        .doOnSuccess(response -> deleteLoginChallengeAsync(loginChallenge, tenantId));
  }

  private Response createCodeResponse(AuthorizeSessionModel session, String code) {
    return new AuthCodeResponseDto(session.getRedirectUri(), session.getState(), code).toResponse();
  }

  private Single<Response> checkExistingConsent(
      AuthorizeSessionModel session, String userId, String tenantId, String loginChallenge) {

    return getUserConsentedScopes(session, userId, tenantId)
        .map(consentedScopes -> hasAllRequiredConsents(session.getAllowedScopes(), consentedScopes))
        .flatMap(
            hasAllConsents ->
                processConsentCheck(session, userId, tenantId, hasAllConsents, loginChallenge));
  }

  private Single<Set<String>> getUserConsentedScopes(
      AuthorizeSessionModel session, String userId, String tenantId) {
    return userConsentDao
        .getUserConsents(tenantId, session.getClient().getClientId(), userId)
        .map(
            existingConsents ->
                existingConsents.stream()
                    .map(UserConsentModel::getScope)
                    .collect(Collectors.toSet()));
  }

  private boolean hasAllRequiredConsents(List<String> requiredScopes, Set<String> consentedScopes) {
    for (String scope : requiredScopes) {
      if (!consentedScopes.contains(scope.trim())) {
        return false;
      }
    }
    return true;
  }

  private Single<Response> processConsentCheck(
      AuthorizeSessionModel session,
      String userId,
      String tenantId,
      boolean hasAllConsents,
      String loginChallenge) {

    if (hasAllConsents) {
      return handleExistingConsent(session, userId, tenantId, loginChallenge);
    } else {
      return handleConsentRequired(session, userId, tenantId);
    }
  }

  private Single<Response> handleExistingConsent(
      AuthorizeSessionModel session, String userId, String tenantId, String loginChallenge) {
    session.setConsentedScopes(session.getAllowedScopes());
    return generateAndStoreAuthorizationCode(session, userId, tenantId)
        .map(code -> createCodeResponse(session, code))
        .doOnSuccess(response -> deleteLoginChallengeAsync(loginChallenge, tenantId));
  }

  private Single<Response> handleConsentRequired(
      AuthorizeSessionModel session, String userId, String tenantId) {

    String consentChallenge = UUID.randomUUID().toString();
    session.setUserId(userId);
    TenantConfig tenantConfig = registry.get(tenantId, TenantConfig.class);
    String consentPageUri =
        tenantConfig.getOidcConfig() != null
            ? tenantConfig.getOidcConfig().getConsentPageUri()
            : null;

    return authorizeSessionDao
        .saveAuthorizeSession(consentChallenge, session, tenantId, 600)
        .andThen(
            Single.just(new LoginAcceptResponseDto(consentPageUri, consentChallenge).toResponse()));
  }

  private Single<String> generateAndStoreAuthorizationCode(
      AuthorizeSessionModel session, String userId, String tenantId) {
    String code = RandomStringUtils.randomAlphanumeric(32);
    session.setUserId(userId);
    CodeSessionModel codeSession = new CodeSessionModel(session);

    return codeSessionDao
        .saveCodeSession(code, codeSession, tenantId, 600)
        .andThen(Single.just(code));
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

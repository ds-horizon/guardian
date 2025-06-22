package com.dreamsportslabs.guardian.service;

import com.dreamsportslabs.guardian.dao.AuthorizeSessionDao;
import com.dreamsportslabs.guardian.dao.CodeSessionDao;
import com.dreamsportslabs.guardian.dao.RefreshTokenDao;
import com.dreamsportslabs.guardian.dao.UserConsentDao;
import com.dreamsportslabs.guardian.dao.model.AuthorizeSessionModel;
import com.dreamsportslabs.guardian.dao.model.CodeSessionModel;
import com.dreamsportslabs.guardian.dto.request.ConsentAcceptRequestDto;
import com.dreamsportslabs.guardian.dto.response.CodeResponseDto;
import com.dreamsportslabs.guardian.exception.OidcErrorEnum;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ConsentAcceptService {
  private final AuthorizeSessionDao authorizeSessionDao;
  private final CodeSessionDao codeSessionDao;
  private final RefreshTokenDao refreshTokenDao;
  private final UserConsentDao userConsentDao;

  public Single<Response> consentAccept(ConsentAcceptRequestDto requestDto, String tenantId) {
    return validateRefreshToken(requestDto.getRefreshToken(), tenantId)
        .flatMap(
            userId ->
                validateConsentChallenge(requestDto.getConsentChallenge(), tenantId)
                    .flatMap(
                        session ->
                            processConsent(
                                session,
                                userId,
                                requestDto,
                                tenantId,
                                requestDto.getConsentChallenge())));
  }

  private Single<String> validateRefreshToken(String refreshToken, String tenantId) {
    return refreshTokenDao
        .getRefreshToken(refreshToken, tenantId)
        .switchIfEmpty(
            Single.error(
                OidcErrorEnum.ACCESS_DENIED.getCustomException(
                    "Invalid refresh token", null, null)));
  }

  private Single<AuthorizeSessionModel> validateConsentChallenge(
      String consentChallenge, String tenantId) {
    return authorizeSessionDao
        .getAuthorizeSession(consentChallenge, tenantId)
        .onErrorResumeNext(
            err ->
                Single.error(
                    OidcErrorEnum.INVALID_REQUEST.getCustomException(
                        "Invalid consent challenge", null, null)));
  }

  private Single<Response> processConsent(
      AuthorizeSessionModel session,
      String userId,
      ConsentAcceptRequestDto requestDto,
      String tenantId,
      String consentChallenge) {

    validateUserMatch(session, userId);

    List<String> finalConsentedScopes =
        calculateFinalConsentedScopes(session, requestDto.getConsentedScopes());

    if (finalConsentedScopes.isEmpty()) {
      return Single.error(
          OidcErrorEnum.INVALID_SCOPE.getCustomException(
              "No valid scopes consented", session.getState(), session.getRedirectUri()));
    }

    session.setConsentedScopes(finalConsentedScopes);

    return saveUserConsents(session, requestDto.getConsentedScopes(), tenantId)
        .andThen(generateAndStoreAuthorizationCode(session, tenantId))
        .map(
            code ->
                new CodeResponseDto(session.getRedirectUri(), session.getState(), code)
                    .toResponse())
        .doOnSuccess(response -> deleteConsentChallengeAsync(consentChallenge, tenantId));
  }

  private void validateUserMatch(AuthorizeSessionModel session, String userId) {
    if (!userId.equals(session.getUserId())) {
      throw OidcErrorEnum.ACCESS_DENIED.getCustomException(
          "Refresh token does not match session user",
          session.getState(),
          session.getRedirectUri());
    }
  }

  private List<String> calculateFinalConsentedScopes(
      AuthorizeSessionModel session, List<String> newConsentedScopes) {
    List<String> validScopes =
        newConsentedScopes.stream()
            .filter(scope -> session.getAllowedScopes().contains(scope))
            .collect(Collectors.toList());

    List<String> existingConsentedScopes =
        session.getConsentedScopes() != null ? session.getConsentedScopes() : new ArrayList<>();

    Set<String> unionScopes = new HashSet<>(existingConsentedScopes);
    unionScopes.addAll(validScopes);

    return new ArrayList<>(unionScopes);
  }

  private Completable saveUserConsents(
      AuthorizeSessionModel session, List<String> consentedScopes, String tenantId) {
    List<Completable> insertOperations =
        consentedScopes.stream()
            .map(
                scope ->
                    userConsentDao.insertConsent(
                        tenantId, session.getClient().getClientId(), session.getUserId(), scope))
            .collect(Collectors.toList());

    return Completable.merge(insertOperations);
  }

  private Single<String> generateAndStoreAuthorizationCode(
      AuthorizeSessionModel session, String tenantId) {
    String code = RandomStringUtils.randomAlphanumeric(32);
    CodeSessionModel codeSession = new CodeSessionModel(session);

    return codeSessionDao
        .saveCodeSession(code, codeSession, tenantId, 600)
        .andThen(Single.just(code));
  }

  private void deleteConsentChallengeAsync(String consentChallenge, String tenantId) {
    if (consentChallenge != null) {
      authorizeSessionDao
          .deleteAuthorizeSession(consentChallenge, tenantId)
          .subscribe(
              () -> log.debug("Successfully deleted consent challenge: {}", consentChallenge),
              error ->
                  log.warn(
                      "Failed to delete consent challenge: {}, error: {}",
                      consentChallenge,
                      error.getMessage()));
    }
  }
}

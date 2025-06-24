package com.dreamsportslabs.guardian.service;

import com.dreamsportslabs.guardian.dao.AuthorizeSessionDao;
import com.dreamsportslabs.guardian.dao.CodeSessionDao;
import com.dreamsportslabs.guardian.dao.RefreshTokenDao;
import com.dreamsportslabs.guardian.dao.UserConsentDao;
import com.dreamsportslabs.guardian.dao.model.AuthorizeSessionModel;
import com.dreamsportslabs.guardian.dao.model.CodeSessionModel;
import com.dreamsportslabs.guardian.dto.request.ConsentAcceptRequestDto;
import com.dreamsportslabs.guardian.dto.response.AuthCodeResponseDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.exception.OidcErrorEnum;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import jakarta.ws.rs.core.Response.ResponseBuilder;
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

  public Single<ResponseBuilder> consentAccept(
      ConsentAcceptRequestDto requestDto, String tenantId) {
    return validateRefreshToken(requestDto.getRefreshToken(), tenantId)
        .flatMap(
            userId ->
                validateConsentChallenge(requestDto.getConsentChallenge(), userId, tenantId)
                    .map(
                        session -> {
                          session.setConsentedScopes(
                              calculateFinalConsentedScopes(
                                  session, requestDto.getConsentedScopes()));
                          return session;
                        })
                    .filter(session -> !session.getConsentedScopes().isEmpty())
                    .switchIfEmpty(
                        Single.error(
                            OidcErrorEnum.INVALID_SCOPE.getCustomException(
                                "No valid scopes consented", null, null)))
                    .flatMap(
                        session ->
                            saveUserConsents(session, requestDto.getConsentedScopes(), tenantId)
                                .andThen(generateAndStoreAuthorizationCode(session, tenantId))
                                .map(
                                    code ->
                                        new AuthCodeResponseDto(
                                                session.getRedirectUri(), session.getState(), code)
                                            .toResponse())
                                .doOnSuccess(
                                    response ->
                                        deleteConsentChallengeAsync(
                                            requestDto.getConsentChallenge(), tenantId))));
  }

  private Single<String> validateRefreshToken(String refreshToken, String tenantId) {
    return refreshTokenDao
        .getRefreshToken(refreshToken, tenantId)
        .switchIfEmpty(
            Single.error(ErrorEnum.UNAUTHORIZED.getCustomException("Invalid refresh token")));
  }

  private Single<AuthorizeSessionModel> validateConsentChallenge(
      String consentChallenge, String userId, String tenantId) {
    return authorizeSessionDao
        .getAuthorizeSession(consentChallenge, tenantId)
        .onErrorResumeNext(
            err ->
                Single.error(
                    ErrorEnum.INVALID_REQUEST.getCustomException("Invalid consent challenge")))
        .filter(session -> userId.equals(session.getUserId()))
        .switchIfEmpty(
            Single.error(
                ErrorEnum.UNAUTHORIZED.getCustomException(
                    "Refresh token does not match session user")));
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

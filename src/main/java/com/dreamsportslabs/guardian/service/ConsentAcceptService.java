package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.SCOPE_OPENID;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.UNAUTHORIZED;

import com.dreamsportslabs.guardian.dao.AuthorizeSessionDao;
import com.dreamsportslabs.guardian.dao.UserConsentDao;
import com.dreamsportslabs.guardian.dao.V1RefreshTokenDao;
import com.dreamsportslabs.guardian.dao.model.AuthorizeSessionModel;
import com.dreamsportslabs.guardian.dao.model.OidcCodeModel;
import com.dreamsportslabs.guardian.dto.request.ConsentAcceptRequestDto;
import com.dreamsportslabs.guardian.dto.response.AuthCodeResponseDto;
import com.dreamsportslabs.guardian.registry.Registry;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ConsentAcceptService {

  private final AuthorizeSessionDao authorizeSessionDao;
  private final OidcCodeService oidcCodeService;
  private final OidcTokenService oidcTokenService;
  private final V1RefreshTokenDao v1RefreshTokenDao;
  private final UserConsentDao userConsentDao;
  private final Registry registry;

  public Single<AuthCodeResponseDto> consentAccept(
      ConsentAcceptRequestDto requestDto, String tenantId) {

    return oidcTokenService
        .validateRefreshToken(requestDto.getRefreshToken(), tenantId)
        .flatMap(
            userId ->
                authorizeSessionDao
                    .getAuthorizeSession(requestDto.getConsentChallenge(), tenantId)
                    .filter(authorizeSession -> userId.equals(authorizeSession.getUserId()))
                    .switchIfEmpty(
                        Single.error(
                            UNAUTHORIZED.getJsonCustomException(
                                "Refresh token does not match session user"))))
        .flatMap(
            authorizeSession -> {
              List<String> allConsentedScopes =
                  getAllConsentedScopes(authorizeSession, requestDto.getConsentedScopes());
              List<String> newlyConsentedScopes =
                  getNewlyConsentedScopes(authorizeSession, allConsentedScopes);
              authorizeSession.setConsentedScopes(allConsentedScopes);
              return userConsentDao
                  .insertConsents(
                      tenantId,
                      authorizeSession.getClient().getClientId(),
                      authorizeSession.getUserId(),
                      newlyConsentedScopes)
                  .toSingleDefault(authorizeSession);
            })
        .flatMap(
            authorizeSession ->
                generateAuthCode(authorizeSession, tenantId)
                    .map(
                        code ->
                            new AuthCodeResponseDto(
                                authorizeSession.getRedirectUri(),
                                authorizeSession.getState(),
                                code)))
        .map(
            res -> {
              deleteConsentChallengeAsync(requestDto.getConsentChallenge(), tenantId);
              return res;
            });
  }

  private List<String> getNewlyConsentedScopes(
      AuthorizeSessionModel authorizeSession, List<String> allConsentedScopes) {
    List<String> allScopes = new ArrayList<>(allConsentedScopes);
    allScopes.removeAll(
        authorizeSession.getConsentedScopes() != null
            ? authorizeSession.getConsentedScopes()
            : new ArrayList<>());
    return allScopes;
  }

  private List<String> getAllConsentedScopes(
      AuthorizeSessionModel authorizeSession, List<String> newScopes) {
    List<String> filteredNewScopes =
        new ArrayList<>(newScopes != null ? newScopes : new ArrayList<>());
    filteredNewScopes.retainAll(authorizeSession.getAllowedScopes());

    Set<String> consentedScopesSet =
        new LinkedHashSet<>(
            authorizeSession.getConsentedScopes() != null
                ? authorizeSession.getConsentedScopes()
                : new ArrayList<>());
    consentedScopesSet.addAll(filteredNewScopes);
    List<String> consentedScopes = new ArrayList<>(consentedScopesSet);

    if (!consentedScopes.contains(SCOPE_OPENID)) {
      throw INVALID_REQUEST.getJsonCustomException("Atleast openid scope has to be consented");
    }

    return consentedScopes;
  }

  private Single<String> generateAuthCode(AuthorizeSessionModel authorizeSession, String tenantId) {
    String code = RandomStringUtils.randomAlphanumeric(32);
    OidcCodeModel oidcCodeModel = new OidcCodeModel(authorizeSession);

    return oidcCodeService.saveOidcCode(code, oidcCodeModel, tenantId).toSingleDefault(code);
  }

  private void deleteConsentChallengeAsync(String consentChallenge, String tenantId) {
    if (consentChallenge != null) {
      authorizeSessionDao
          .deleteAuthorizeSession(consentChallenge, tenantId)
          .subscribe(
              () -> log.info("Successfully deleted consent challenge: {}", consentChallenge),
              error ->
                  log.warn(
                      "Failed to delete consent challenge: {}, error: {}",
                      consentChallenge,
                      error.getMessage()));
    }
  }
}

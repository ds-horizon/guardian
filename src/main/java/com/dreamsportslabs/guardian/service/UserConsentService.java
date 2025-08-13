package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_TOKEN;

import com.dreamsportslabs.guardian.dao.AuthorizeSessionDao;
import com.dreamsportslabs.guardian.dao.UserConsentDao;
import com.dreamsportslabs.guardian.dao.model.AuthorizeSessionModel;
import com.dreamsportslabs.guardian.dao.model.UserConsentModel;
import com.dreamsportslabs.guardian.dto.request.UserConsentRequestDto;
import com.dreamsportslabs.guardian.dto.response.UserConsentResponseDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class UserConsentService {

  private final AuthorizeSessionDao authorizeSessionDao;
  private final UserConsentDao userConsentDao;
  private final OidcTokenService oidcTokenService;

  public Single<UserConsentResponseDto> getUserConsent(
      UserConsentRequestDto userConsentRequestDto, String tenantId) {
    return oidcTokenService
        .validateRefreshToken(userConsentRequestDto.getRefreshToken(), tenantId)
        .flatMap(
            userId ->
                authorizeSessionDao
                    .getAuthorizeSession(userConsentRequestDto.getConsentChallenge(), tenantId)
                    .filter(authorizeSession -> userId.equals(authorizeSession.getUserId()))
                    .switchIfEmpty(Single.error(INVALID_TOKEN.getException()))
                    .flatMap(
                        authorizeSession -> {
                          String clientId = authorizeSession.getClient().getClientId();
                          return userConsentDao
                              .getUserConsents(tenantId, clientId, userId)
                              .map(
                                  userConsents ->
                                      buildUserConsentResponse(authorizeSession, userConsents));
                        }));
  }

  private UserConsentResponseDto buildUserConsentResponse(
      AuthorizeSessionModel authorizeSession, List<UserConsentModel> userConsents) {

    List<String> consentedScopes = userConsents.stream().map(UserConsentModel::getScope).toList();

    return UserConsentResponseDto.builder()
        .client(authorizeSession.getClient())
        .requestedScopes(authorizeSession.getAllowedScopes())
        .consentedScopes(consentedScopes)
        .subject(authorizeSession.getUserId())
        .build();
  }
}

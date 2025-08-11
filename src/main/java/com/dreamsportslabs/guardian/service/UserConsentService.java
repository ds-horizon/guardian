package com.dreamsportslabs.guardian.service;

import com.dreamsportslabs.guardian.dao.AuthorizeSessionDao;
import com.dreamsportslabs.guardian.dao.UserConsentDao;
import com.dreamsportslabs.guardian.dao.model.AuthorizeSessionModel;
import com.dreamsportslabs.guardian.dao.model.UserConsentModel;
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

  public Single<UserConsentResponseDto> getUserConsent(String consentChallenge, String tenantId) {
    return authorizeSessionDao
        .getAuthorizeSession(consentChallenge, tenantId)
        .flatMap(
            authorizeSession -> {
              String userId = authorizeSession.getUserId();
              String clientId = authorizeSession.getClient().getClientId();

              return userConsentDao
                  .getUserConsents(tenantId, clientId, userId)
                  .map(userConsents -> buildUserConsentResponse(authorizeSession, userConsents));
            });
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

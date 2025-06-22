package com.dreamsportslabs.guardian.service;

import com.dreamsportslabs.guardian.exception.OidcErrorEnum;

import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.dao.AuthorizeSessionDao;
import com.dreamsportslabs.guardian.dao.CodeSessionDao;
import com.dreamsportslabs.guardian.dao.RefreshTokenDao;
import com.dreamsportslabs.guardian.dao.UserConsentDao;
import com.dreamsportslabs.guardian.dao.model.AuthorizeSessionModel;
import com.dreamsportslabs.guardian.dao.model.CodeSessionModel;
import com.dreamsportslabs.guardian.dao.model.UserConsentModel;
import com.dreamsportslabs.guardian.dto.request.LoginAcceptRequestDto;
import com.dreamsportslabs.guardian.dto.response.LoginAcceptResponseDto;
import com.dreamsportslabs.guardian.registry.Registry;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import jakarta.ws.rs.core.MultivaluedMap;
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
  private final ClientService clientService;
  private final Registry registry;

  public Single<LoginAcceptResponseDto> loginAccept(
      LoginAcceptRequestDto requestDto, MultivaluedMap<String, String> headers, String tenantId) {

    return authorizeSessionDao
        .getAuthorizeSession(requestDto.getLoginChallenge(), tenantId)
        .onErrorResumeNext(err -> Single.error(OidcErrorEnum.INVALID_REQUEST.getCustomException("Invalid login challenge", null, null)))
        .flatMap(session -> refreshTokenDao
            .getRefreshToken(requestDto.getRefreshToken(), tenantId)
            .switchIfEmpty(Single.error(OidcErrorEnum.ACCESS_DENIED.getCustomException("Invalid refresh token", session.getState(), session.getRedirectUri())))
            .flatMap(userId -> {
              session.setUserId(userId);
              return authorizeSessionDao
                  .saveAuthorizeSession(session, tenantId, 600)
                  .andThen(Single.just(session));
            })
            .flatMap(sessionWithUser -> clientService
                .getClient(sessionWithUser.getClient().getClientId(), tenantId)
                .switchIfEmpty(Single.error(OidcErrorEnum.INVALID_REQUEST.getCustomException("Invalid client", sessionWithUser.getState(), sessionWithUser.getRedirectUri())))
                .flatMap(client -> {
                  if (Boolean.TRUE.equals(client.getSkipConsent())) {
                    sessionWithUser.setConsentedScopes(sessionWithUser.getAllowedScopes());
                    return generateAndStoreAuthorizationCode(sessionWithUser, tenantId)
                        .map(code -> new LoginAcceptResponseDto(sessionWithUser.getRedirectUri(), sessionWithUser.getState(), code));
                  } else {
                    return checkExistingConsent(sessionWithUser, sessionWithUser.getUserId(), tenantId);
                  }
                })));
  }

  private Single<LoginAcceptResponseDto> checkExistingConsent(
      AuthorizeSessionModel session, String userId, String tenantId) {
    
    return userConsentDao
        .getUserConsents(tenantId, session.getClient().getClientId(), userId)
        .map(existingConsents -> {
          Set<String> consentedScopes = existingConsents.stream()
              .map(UserConsentModel::getScope)
              .collect(Collectors.toSet());
          
          boolean hasAllConsents = true;
          for (String scope : session.getAllowedScopes()) {
            if (!consentedScopes.contains(scope.trim())) {
              hasAllConsents = false;
              break;
            }
          }
          return hasAllConsents;
        })
        .flatMap(hasAllConsents -> {
          if (hasAllConsents) {
            session.setConsentedScopes(session.getAllowedScopes());
            return generateAndStoreAuthorizationCode(session, tenantId)
                .map(code -> new LoginAcceptResponseDto(session.getRedirectUri(), session.getState(), code));
          } else {
            return handleConsentRequired(session, tenantId);
          }
        });
  }

  private Single<LoginAcceptResponseDto> handleConsentRequired(
      AuthorizeSessionModel session, String tenantId) {
    
    String consentChallenge = UUID.randomUUID().toString();
    TenantConfig tenantConfig = registry.get(tenantId, TenantConfig.class);
    String consentPageUri = tenantConfig.getOidcConfig() != null 
        ? tenantConfig.getOidcConfig().getConsentPageUri() 
        : null;
    
    return authorizeSessionDao
        .saveAuthorizeSession(session, tenantId, 600)
        .andThen(Single.just(new LoginAcceptResponseDto(consentPageUri, consentChallenge)));
  }

  private Single<String> generateAndStoreAuthorizationCode(AuthorizeSessionModel session, String tenantId) {
    String code = RandomStringUtils.randomAlphanumeric(32);
    CodeSessionModel codeSession = new CodeSessionModel(session);
    
    return codeSessionDao
        .saveCodeSession(code, codeSession, tenantId, 600)
        .andThen(Single.just(code));
  }
} 
package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.UNAUTHORIZED;
import static com.dreamsportslabs.guardian.utils.Utils.getCurrentTimeInSeconds;

import com.dreamsportslabs.guardian.dao.RefreshTokenDao;
import com.dreamsportslabs.guardian.dao.SsoTokenDao;
import com.dreamsportslabs.guardian.dao.model.SsoTokenModel;
import com.dreamsportslabs.guardian.dto.request.LoginAcceptRequestDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class SsoTokenService {
  private final SsoTokenDao ssoTokenDao;
  private final RefreshTokenDao refreshTokenDao;

  public Single<SsoTokenModel> validateUserSession(
      LoginAcceptRequestDto requestDto, String tenantId) {
    if (StringUtils.isNotBlank(requestDto.getSsoToken())) {
      return ssoTokenDao
          .getSsoToken(requestDto.getSsoToken(), tenantId)
          .switchIfEmpty(Single.error(UNAUTHORIZED.getJsonCustomException("Invalid sso token")))
          .filter(ssoTokenModel -> ssoTokenModel.getExpiry() > getCurrentTimeInSeconds())
          .switchIfEmpty(Single.error(UNAUTHORIZED.getException()));
    } else if (StringUtils.isNotBlank(requestDto.getRefreshToken())) {
      return ssoTokenDao
          .getSsoTokenFromRefreshToken(requestDto.getRefreshToken(), tenantId)
          // ToDo: Need to remove this post deprecation of v1 APIs migration
          .switchIfEmpty(
              refreshTokenDao
                  .getRefreshToken(tenantId, requestDto.getRefreshToken())
                  .map(
                      refreshTokenModel ->
                          SsoTokenModel.builder()
                              .authMethods(refreshTokenModel.getAuthMethod())
                              .refreshToken(refreshTokenModel.getRefreshToken())
                              .userId(refreshTokenModel.getUserId())
                              .expiry(refreshTokenModel.getRefreshTokenExp())
                              .clientIdIssuedTo(refreshTokenModel.getClientId())
                              .tenantId(refreshTokenModel.getTenantId())
                              .isActive(refreshTokenModel.getIsActive())
                              .build()))
          .switchIfEmpty(Single.error(UNAUTHORIZED.getJsonCustomException("Invalid refresh token")))
          .filter(ssoTokenModel -> ssoTokenModel.getExpiry() > getCurrentTimeInSeconds())
          .switchIfEmpty(
              Single.error(UNAUTHORIZED.getJsonCustomException("Invalid refresh token")));
    } else {
      return Single.error(UNAUTHORIZED.getJsonCustomException("No valid session token found"));
    }
  }
}

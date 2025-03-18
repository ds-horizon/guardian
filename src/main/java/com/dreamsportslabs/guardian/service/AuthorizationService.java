package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.CODE;
import static com.dreamsportslabs.guardian.constant.Constants.COOKIE;
import static com.dreamsportslabs.guardian.constant.Constants.SET_COOKIE;
import static com.dreamsportslabs.guardian.constant.Constants.TOKEN;
import static com.dreamsportslabs.guardian.constant.Constants.TOKEN_TYPE;
import static com.dreamsportslabs.guardian.constant.Constants.USERID;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_CODE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.UNAUTHORIZED;

import com.dreamsportslabs.guardian.config.tenant.AuthCodeConfig;
import com.dreamsportslabs.guardian.config.tenant.CookieConfig;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.config.tenant.TokenConfig;
import com.dreamsportslabs.guardian.dao.CodeDao;
import com.dreamsportslabs.guardian.dao.CookieDao;
import com.dreamsportslabs.guardian.dao.RefreshTokenDao;
import com.dreamsportslabs.guardian.dao.RevocationDao;
import com.dreamsportslabs.guardian.dao.model.CodeModel;
import com.dreamsportslabs.guardian.dao.model.CookieModel;
import com.dreamsportslabs.guardian.dao.model.RefreshTokenModel;
import com.dreamsportslabs.guardian.dto.request.MetaInfo;
import com.dreamsportslabs.guardian.dto.request.V1CodeTokenExchangeRequestDto;
import com.dreamsportslabs.guardian.dto.request.V1LogoutRequestDto;
import com.dreamsportslabs.guardian.dto.request.V1RefreshTokenRequestDto;
import com.dreamsportslabs.guardian.dto.response.CodeResponseDto;
import com.dreamsportslabs.guardian.dto.response.RefreshTokenResponseDto;
import com.dreamsportslabs.guardian.dto.response.TokenResponseDto;
import com.dreamsportslabs.guardian.registry.Registry;
import com.dreamsportslabs.guardian.utils.Utils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class AuthorizationService {
  private final Registry registry;
  private final TokenIssuer tokenIssuer;

  private final RefreshTokenDao refreshTokenDao;
  private final CodeDao codeDao;
  private final CookieDao cookieDao;
  private final RevocationDao revocationDao;

  public Single<Response> generate(
      JsonObject user, String responseType, MetaInfo metaInfo, String tenantId) {
    return switch (responseType) {
      case TOKEN -> generateTokens(user, metaInfo, tenantId).map(dto -> Response.ok(dto).build());
      case COOKIE -> generateCookie(user, metaInfo, tenantId)
          .map(cookie -> Response.noContent().header(SET_COOKIE, cookie).build());
      case CODE -> generateCode(user, metaInfo, tenantId).map(dto -> Response.ok(dto).build());
      default -> throw INVALID_REQUEST.getException();
    };
  }

  private Single<TokenResponseDto> generateTokens(
      JsonObject user, MetaInfo metaInfo, String tenantId) {
    TenantConfig config = registry.get(tenantId, TenantConfig.class);
    String refreshToken = tokenIssuer.generateRefreshToken();
    Long iat = System.currentTimeMillis() / 1000;
    return Single.zip(
            tokenIssuer.generateAccessToken(
                user.getString(USERID), iat, getRftId(refreshToken), config),
            tokenIssuer.generateIdToken(user, iat, config),
            (accessToken, idToken) ->
                new TokenResponseDto(
                    accessToken,
                    refreshToken,
                    idToken,
                    TOKEN_TYPE,
                    config.getTokenConfig().getAccessTokenExpiry(),
                    user.getBoolean("isNewUser", false)))
        .flatMap(
            dto ->
                refreshTokenDao
                    .saveRefreshToken(getRefreshTokenDto(refreshToken, user, iat, metaInfo, config))
                    .andThen(Single.just(dto)));
  }

  private RefreshTokenModel getRefreshTokenDto(
      String refreshToken, JsonObject user, Long iat, MetaInfo metaInfo, TenantConfig config) {
    return RefreshTokenModel.builder()
        .tenantId(config.getTenantId())
        .userId(user.getString(USERID))
        .refreshToken(refreshToken)
        .refreshTokenExp(iat + config.getTokenConfig().getRefreshTokenExpiry())
        .deviceName(metaInfo.getDeviceName())
        .ip(metaInfo.getIp())
        .location(metaInfo.getLocation())
        .source(metaInfo.getSource())
        .build();
  }

  public Single<RefreshTokenResponseDto> refreshTokens(
      V1RefreshTokenRequestDto dto, String tenantId) {
    TenantConfig config = registry.get(tenantId, TenantConfig.class);
    return refreshTokenDao
        .getRefreshToken(dto.getRefreshToken(), tenantId)
        .switchIfEmpty(Single.error(UNAUTHORIZED.getCustomException("Invalid refresh token")))
        .flatMap(
            userId ->
                tokenIssuer.generateAccessToken(
                    userId,
                    System.currentTimeMillis() / 1000,
                    getRftId(dto.getRefreshToken()),
                    config))
        .map(
            accessToken ->
                new RefreshTokenResponseDto(
                    accessToken, TOKEN_TYPE, config.getTokenConfig().getAccessTokenExpiry()));
  }

  private String getRftId(String refreshToken) {
    return Utils.getMd5Hash(refreshToken);
  }

  private Single<String> generateCookie(JsonObject user, MetaInfo metaInfo, String tenantId) {
    CookieConfig config = registry.get(tenantId, TenantConfig.class).getCookieConfig();
    String cookieValue = generateCookie();
    return cookieDao
        .saveCookie(
            CookieModel.builder()
                .tenantId(tenantId)
                .userId(user.getString(USERID))
                .cookieName(config.getCookieName())
                .cookieValue(cookieValue)
                .cookieExp(System.currentTimeMillis() / 1000 + config.getMaxAge())
                .domain(config.getDomain())
                .path(config.getPath())
                .sameSite(config.getSameSite().toString())
                .isActive(true)
                .source(metaInfo.getSource())
                .deviceName(metaInfo.getDeviceName())
                .location(metaInfo.getLocation())
                .ip(metaInfo.getIp())
                .build())
        .map(
            cookieModel ->
                new NewCookie.Builder(config.getCookieName())
                    .value(cookieValue)
                    .path(config.getPath())
                    .domain(config.getDomain())
                    .version(1)
                    .maxAge(config.getMaxAge())
                    .secure(true)
                    .httpOnly(true)
                    .sameSite(config.getSameSite())
                    .build()
                    .toString());
  }

  private String generateCookie() {
    return RandomStringUtils.randomAlphanumeric(32);
  }

  private Single<CodeResponseDto> generateCode(
      JsonObject user, MetaInfo metaInfo, String tenantId) {
    AuthCodeConfig config = registry.get(tenantId, TenantConfig.class).getAuthCodeConfig();
    String code = RandomStringUtils.randomAlphanumeric(config.getLength());
    CodeModel codeModel =
        CodeModel.builder()
            .user(user.getMap())
            .code(code)
            .metaInfo(metaInfo)
            .expiry(config.getTtl())
            .build();
    return codeDao
        .saveCode(codeModel, tenantId)
        .andThen(Single.just(new CodeResponseDto(code, config.getTtl())));
  }

  public Single<TokenResponseDto> codeTokenExchange(
      V1CodeTokenExchangeRequestDto dto, String tenantId) {
    return codeDao
        .getCode(dto.getCode(), tenantId)
        .switchIfEmpty(Single.error(INVALID_CODE.getException()))
        .flatMap(
            model -> generateTokens(new JsonObject(model.getUser()), model.getMetaInfo(), tenantId))
        .doOnSuccess(res -> codeDao.deleteCode(dto.getCode(), tenantId).subscribe());
  }

  public Completable logout(V1LogoutRequestDto requestDto, String tenantId) {
    if (requestDto.getRefreshToken() != null) {
      return invalidateRefreshToken(requestDto, tenantId);
    } else {
      return invalidateCookie(requestDto, tenantId);
    }
  }

  private Completable invalidateRefreshToken(V1LogoutRequestDto dto, String tenantId) {
    return refreshTokenDao
        .getRefreshToken(dto.getRefreshToken(), tenantId)
        .switchIfEmpty(Single.error(UNAUTHORIZED.getCustomException("Invalid refresh token")))
        .flatMapCompletable(
            userId -> {
              if (dto.getIsUniversalLogout()) {
                return invalidateAllSessions(userId, tenantId)
                    .doOnSuccess(tokens -> updateRevocations(tokens, tenantId))
                    .ignoreElement();
              } else {
                return refreshTokenDao
                    .invalidateRefreshToken(dto.getRefreshToken(), tenantId)
                    .doOnComplete(
                        () -> updateRevocations(List.of(dto.getRefreshToken()), tenantId));
              }
            });
  }

  private Completable invalidateCookie(V1LogoutRequestDto dto, String tenantId) {
    return cookieDao
        .getCookie(dto.getCookie(), tenantId)
        .switchIfEmpty(Single.error(UNAUTHORIZED.getCustomException("Invalid cookie")))
        .flatMapCompletable(
            userId -> {
              if (dto.getIsUniversalLogout()) {
                return invalidateAllSessions(userId, tenantId)
                    .doOnSuccess(tokens -> updateRevocations(tokens, tenantId))
                    .ignoreElement();
              } else {
                return cookieDao.invalidateCookie(dto.getCookie(), tenantId);
              }
            });
  }

  private Single<List<String>> invalidateAllSessions(String userId, String tenantId) {
    return cookieDao
        .invalidateAllCookiesForUser(userId, tenantId)
        .andThen(
            refreshTokenDao
                .getRefreshTokens(userId, tenantId)
                .flatMap(
                    list ->
                        refreshTokenDao
                            .invalidateAllRefreshTokensForUser(userId, tenantId)
                            .andThen(Single.just(list))));
  }

  private void updateRevocations(List<String> refreshTokens, String tenantId) {
    TokenConfig config = registry.get(tenantId, TenantConfig.class).getTokenConfig();
    List<String> expiredRefreshTokens = new ArrayList<>();

    for (String refreshToken : refreshTokens) {
      String rftId = getRftId(refreshToken);
      expiredRefreshTokens.add(rftId);
    }
    long currentTimeStamp = System.currentTimeMillis() / 1000;

    long accessTokenExpiry = config.getAccessTokenExpiry() * 60;

    revocationDao.addExpiredRefreshTokensInSortedSet(
        currentTimeStamp, expiredRefreshTokens, tenantId);

    revocationDao.removeExpiredRefreshTokensFromSortedSet(
        currentTimeStamp, accessTokenExpiry, tenantId);
  }
}

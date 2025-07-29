package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.ACCESS_TOKEN_COOKIE_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.CODE;
import static com.dreamsportslabs.guardian.constant.Constants.IS_NEW_USER;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_EXP;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_IAT;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_ISS;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_RFT_ID;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_SUB;
import static com.dreamsportslabs.guardian.constant.Constants.REFRESH_TOKEN_COOKIE_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.TOKEN;
import static com.dreamsportslabs.guardian.constant.Constants.TOKEN_TYPE;
import static com.dreamsportslabs.guardian.constant.Constants.USERID;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_CODE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.UNAUTHORIZED;
import static com.dreamsportslabs.guardian.utils.Utils.getCurrentTimeInSeconds;
import static com.dreamsportslabs.guardian.utils.Utils.getRftId;

import com.dreamsportslabs.guardian.config.tenant.AuthCodeConfig;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.config.tenant.TokenConfig;
import com.dreamsportslabs.guardian.dao.CodeDao;
import com.dreamsportslabs.guardian.dao.RefreshTokenDao;
import com.dreamsportslabs.guardian.dao.RevocationDao;
import com.dreamsportslabs.guardian.dao.model.CodeModel;
import com.dreamsportslabs.guardian.dao.model.RefreshTokenModel;
import com.dreamsportslabs.guardian.dto.request.MetaInfo;
import com.dreamsportslabs.guardian.dto.request.V1CodeTokenExchangeRequestDto;
import com.dreamsportslabs.guardian.dto.request.V1LogoutRequestDto;
import com.dreamsportslabs.guardian.dto.request.V1RefreshTokenRequestDto;
import com.dreamsportslabs.guardian.dto.response.CodeResponseDto;
import com.dreamsportslabs.guardian.dto.response.IdpConnectResponseDto;
import com.dreamsportslabs.guardian.dto.response.RefreshTokenResponseDto;
import com.dreamsportslabs.guardian.dto.response.TokenResponseDto;
import com.dreamsportslabs.guardian.registry.Registry;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.core.NewCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  private final RevocationDao revocationDao;

  public Single<Object> generate(
      JsonObject user, String responseType, MetaInfo metaInfo, String tenantId) {
    if (responseType.equals(TOKEN)) {
      return generateTokens(user, metaInfo, tenantId).map(res -> res);
    } else if (responseType.equals(CODE)) {
      return generateCode(user, metaInfo, tenantId).map(res -> res);
    }
    throw INVALID_REQUEST.getException();
  }

  public NewCookie[] getCookies(TokenResponseDto responseDto, String tenantId) {
    NewCookie accessTokenCookie = getAccessTokenCookie(responseDto.getAccessToken(), tenantId);
    NewCookie refreshTokenCookie = getRefreshTokenCookie(responseDto.getRefreshToken(), tenantId);
    return new NewCookie[] {accessTokenCookie, refreshTokenCookie};
  }

  public NewCookie[] getIDPConnectCookies(IdpConnectResponseDto responseDto, String tenantId) {
    NewCookie accessTokenCookie = getAccessTokenCookie(responseDto.getAccessToken(), tenantId);
    NewCookie refreshTokenCookie = getRefreshTokenCookie(responseDto.getRefreshToken(), tenantId);
    return new NewCookie[] {accessTokenCookie, refreshTokenCookie};
  }

  private Single<TokenResponseDto> generateTokens(
      JsonObject user, MetaInfo metaInfo, String tenantId) {
    TenantConfig config = registry.get(tenantId, TenantConfig.class);
    String refreshToken = tokenIssuer.generateRefreshToken();
    Long iat = getCurrentTimeInSeconds();
    Map<String, Object> commonTokenClaims = new HashMap<>();
    commonTokenClaims.put(JWT_CLAIMS_SUB, user.getString(USERID));
    commonTokenClaims.put(JWT_CLAIMS_IAT, iat);
    commonTokenClaims.put(JWT_CLAIMS_ISS, config.getTokenConfig().getIssuer());
    Map<String, Object> accessTokenClaims = new HashMap<>(commonTokenClaims),
        idTokenClaims = new HashMap<>(commonTokenClaims);
    accessTokenClaims.put(JWT_CLAIMS_RFT_ID, getRftId(refreshToken));
    accessTokenClaims.put(JWT_CLAIMS_EXP, iat + config.getTokenConfig().getAccessTokenExpiry());
    idTokenClaims.put(JWT_CLAIMS_EXP, iat + config.getTokenConfig().getIdTokenExpiry());
    return Single.zip(
            tokenIssuer.generateAccessToken(accessTokenClaims, config.getTenantId()),
            tokenIssuer.generateIdToken(idTokenClaims, user, config.getTenantId()),
            (accessToken, idToken) ->
                new TokenResponseDto(
                    accessToken,
                    refreshToken,
                    idToken,
                    TOKEN_TYPE,
                    config.getTokenConfig().getAccessTokenExpiry(),
                    user.getBoolean(IS_NEW_USER, false)))
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
        .getUserIdFromRefreshToken(dto.getRefreshToken(), tenantId)
        .switchIfEmpty(Single.error(UNAUTHORIZED.getCustomException("Invalid refresh token")))
        .flatMap(
            userId ->
                tokenIssuer.generateAccessToken(
                    getAccessTokenClaims(
                        userId, getCurrentTimeInSeconds(), config, dto.getRefreshToken()),
                    config.getTenantId()))
        .map(
            accessToken ->
                new RefreshTokenResponseDto(
                    accessToken, TOKEN_TYPE, config.getTokenConfig().getAccessTokenExpiry()));
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
        .andThen(
            Single.just(
                new CodeResponseDto(code, config.getTtl(), user.getBoolean(IS_NEW_USER, false))));
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
    return invalidateRefreshToken(requestDto, tenantId);
  }

  public Completable adminLogout(String userId, String tenantId) {
    return refreshTokenDao
        .getRefreshTokens(userId, tenantId)
        .flatMap(
            list ->
                refreshTokenDao
                    .invalidateAllRefreshTokensForUser(userId, tenantId)
                    .andThen(Single.just(list)))
        .doOnSuccess(tokens -> updateRevocations(tokens, tenantId))
        .ignoreElement();
  }

  private Completable invalidateRefreshToken(V1LogoutRequestDto dto, String tenantId) {
    return refreshTokenDao
        .getUserIdFromRefreshToken(dto.getRefreshToken(), tenantId)
        .switchIfEmpty(Single.error(UNAUTHORIZED.getCustomException("Invalid refresh token")))
        .flatMapCompletable(
            userId -> {
              if (dto.getIsUniversalLogout()) {
                return refreshTokenDao
                    .getRefreshTokens(userId, tenantId)
                    .flatMap(
                        list ->
                            refreshTokenDao
                                .invalidateAllRefreshTokensForUser(userId, tenantId)
                                .andThen(Single.just(list)))
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

  private void updateRevocations(List<String> refreshTokens, String tenantId) {
    TokenConfig config = registry.get(tenantId, TenantConfig.class).getTokenConfig();
    List<String> expiredRefreshTokens = new ArrayList<>();

    for (String refreshToken : refreshTokens) {
      String rftId = getRftId(refreshToken);
      expiredRefreshTokens.add(rftId);
    }
    long currentTimeStamp = getCurrentTimeInSeconds();

    long accessTokenExpiry = config.getAccessTokenExpiry() * 60;

    revocationDao.addExpiredRefreshTokensInSortedSet(
        currentTimeStamp, expiredRefreshTokens, tenantId);

    revocationDao.removeExpiredRefreshTokensFromSortedSet(
        currentTimeStamp, accessTokenExpiry, tenantId);
  }

  protected NewCookie buildCookie(String name, String value, Integer maxAge, String tenantId) {
    TenantConfig config = registry.get(tenantId, TenantConfig.class);
    return new NewCookie.Builder(name)
        .value(value)
        .path(config.getTokenConfig().getCookiePath())
        .maxAge(maxAge)
        .domain(config.getTokenConfig().getCookieDomain())
        .sameSite(NewCookie.SameSite.valueOf(config.getTokenConfig().getCookieSameSite()))
        .httpOnly(config.getTokenConfig().getCookieHttpOnly())
        .secure(config.getTokenConfig().getCookieSecure())
        .build();
  }

  public NewCookie getAccessTokenCookie(String accessToken, String tenantId) {
    return buildCookie(
        ACCESS_TOKEN_COOKIE_NAME,
        accessToken,
        registry.get(tenantId, TenantConfig.class).getTokenConfig().getAccessTokenExpiry(),
        tenantId);
  }

  public NewCookie getRefreshTokenCookie(String refreshToken, String tenantId) {
    return buildCookie(
        REFRESH_TOKEN_COOKIE_NAME,
        refreshToken,
        registry.get(tenantId, TenantConfig.class).getTokenConfig().getRefreshTokenExpiry(),
        tenantId);
  }

  private Map<String, Object> getAccessTokenClaims(
      String userId, long iat, TenantConfig config, String refreshToken) {
    Map<String, Object> accessTokenClaims = new HashMap<>();
    accessTokenClaims.put(JWT_CLAIMS_SUB, userId);
    accessTokenClaims.put(JWT_CLAIMS_IAT, iat);
    accessTokenClaims.put(JWT_CLAIMS_ISS, config.getTokenConfig().getIssuer());
    accessTokenClaims.put(JWT_CLAIMS_RFT_ID, getRftId(refreshToken));
    accessTokenClaims.put(JWT_CLAIMS_EXP, iat + config.getTokenConfig().getAccessTokenExpiry());
    return accessTokenClaims;
  }
}

package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.ACCESS_TOKEN_COOKIE_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.CODE;
import static com.dreamsportslabs.guardian.constant.Constants.IS_NEW_USER;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_EXP;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_IAT;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_ISS;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_RFT_ID;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_SUB;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_TENANT_ID_CLAIM;
import static com.dreamsportslabs.guardian.constant.Constants.REFRESH_TOKEN_COOKIE_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.SSO_TOKEN_COOKIE_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.TOKEN;
import static com.dreamsportslabs.guardian.constant.Constants.TOKEN_TYPE;
import static com.dreamsportslabs.guardian.constant.Constants.USERID;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_CODE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.UNAUTHORIZED;
import static com.dreamsportslabs.guardian.utils.Utils.appendAdditionalAccessTokenClaims;
import static com.dreamsportslabs.guardian.utils.Utils.getCurrentTimeInSeconds;
import static com.dreamsportslabs.guardian.utils.Utils.getRftId;
import static com.dreamsportslabs.guardian.utils.Utils.shouldSetAccessTokenAdditionalClaims;

import com.dreamsportslabs.guardian.config.tenant.AuthCodeConfig;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.config.tenant.TokenConfig;
import com.dreamsportslabs.guardian.constant.AuthMethod;
import com.dreamsportslabs.guardian.dao.CodeDao;
import com.dreamsportslabs.guardian.dao.OidcRefreshTokenDao;
import com.dreamsportslabs.guardian.dao.RefreshTokenDao;
import com.dreamsportslabs.guardian.dao.RevocationDao;
import com.dreamsportslabs.guardian.dao.model.CodeModel;
import com.dreamsportslabs.guardian.dao.model.OidcRefreshTokenModel;
import com.dreamsportslabs.guardian.dao.model.SsoTokenModel;
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
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class AuthorizationService {
  private final Registry registry;
  private final TokenIssuer tokenIssuer;

  private final RefreshTokenDao refreshTokenDao;
  private final OidcRefreshTokenDao oidcRefreshTokenDao;
  private final CodeDao codeDao;
  private final RevocationDao revocationDao;
  private final UserService userService;

  public Single<Object> generate(
      JsonObject user,
      String responseType,
      String scopes,
      List<AuthMethod> authMethods,
      MetaInfo metaInfo,
      String clientId,
      String tenantId) {
    if (responseType.equals(TOKEN)) {
      return generateTokens(user, scopes, authMethods, metaInfo, clientId, tenantId)
          .map(res -> res);
    } else if (responseType.equals(CODE)) {
      return generateCode(user, metaInfo, tenantId).map(res -> res);
    }
    throw INVALID_REQUEST.getException();
  }

  public NewCookie[] getCookies(TokenResponseDto responseDto, String tenantId) {
    NewCookie accessTokenCookie = getAccessTokenCookie(responseDto.getAccessToken(), tenantId);
    NewCookie refreshTokenCookie = getRefreshTokenCookie(responseDto.getRefreshToken(), tenantId);
    NewCookie ssoTokenTokenCookie = getSsoTokenCookie(responseDto.getSsoToken(), tenantId);
    return new NewCookie[] {accessTokenCookie, refreshTokenCookie, ssoTokenTokenCookie};
  }

  public NewCookie[] getGuestAccessTokenCookies(String accessToken, String tenantId) {
    NewCookie accessTokenCookie = getAccessTokenCookie(accessToken, tenantId);
    return new NewCookie[] {accessTokenCookie};
  }

  public NewCookie[] getIDPConnectCookies(IdpConnectResponseDto responseDto, String tenantId) {
    NewCookie accessTokenCookie = getAccessTokenCookie(responseDto.getAccessToken(), tenantId);
    NewCookie refreshTokenCookie = getRefreshTokenCookie(responseDto.getRefreshToken(), tenantId);
    return new NewCookie[] {accessTokenCookie, refreshTokenCookie};
  }

  private Single<TokenResponseDto> generateTokens(
      JsonObject user,
      String scopes,
      List<AuthMethod> authMethods,
      MetaInfo metaInfo,
      String clientId,
      String tenantId) {
    TenantConfig config = registry.get(tenantId, TenantConfig.class);
    String refreshToken = tokenIssuer.generateRefreshToken();
    String ssoToken = tokenIssuer.generateSsoToken();
    long iat = getCurrentTimeInSeconds();
    return Single.zip(
            tokenIssuer.generateAccessToken(
                refreshToken, iat, scopes, user, authMethods, clientId, tenantId, config),
            tokenIssuer.generateIdToken(
                iat,
                null,
                user,
                config.getTokenConfig().getIdTokenClaims(),
                clientId,
                config.getTenantId()),
            (accessToken, idToken) ->
                new TokenResponseDto(
                    accessToken,
                    refreshToken,
                    idToken,
                    ssoToken,
                    TOKEN_TYPE,
                    config.getTokenConfig().getAccessTokenExpiry(),
                    user.getBoolean(IS_NEW_USER, false)))
        .flatMap(
            dto ->
                oidcRefreshTokenDao
                    .saveOidcRefreshToken(
                        getRefreshTokenDto(
                            refreshToken,
                            user,
                            iat,
                            Arrays.stream(StringUtils.split(scopes, " ")).toList(),
                            metaInfo,
                            clientId,
                            config),
                        getSsoTokenDto(
                            user.getString(USERID), ssoToken, iat, refreshToken, clientId, config))
                    .andThen(Single.just(dto)));
  }

  private OidcRefreshTokenModel getRefreshTokenDto(
      String refreshToken,
      JsonObject user,
      Long iat,
      List<String> scopes,
      MetaInfo metaInfo,
      String clientId,
      TenantConfig config) {
    return OidcRefreshTokenModel.builder()
        .tenantId(config.getTenantId())
        .clientId(clientId)
        .userId(user.getString(USERID))
        .refreshToken(refreshToken)
        .refreshTokenExp(iat + config.getTokenConfig().getRefreshTokenExpiry())
        .deviceName(metaInfo.getDeviceName())
        .ip(metaInfo.getIp())
        .location(metaInfo.getLocation())
        .source(metaInfo.getSource())
        .scope(scopes)
        .build();
  }

  private SsoTokenModel getSsoTokenDto(
      String userId,
      String ssoToken,
      Long iat,
      String refreshToken,
      String clientId,
      TenantConfig config) {
    return SsoTokenModel.builder()
        .tenantId(config.getTenantId())
        .clientIdIssuedTo(clientId)
        .userId(userId)
        .refreshToken(refreshToken)
        .ssoToken(ssoToken)
        .expiry(iat + config.getTokenConfig().getRefreshTokenExpiry())
        .build();
  }

  public Single<String> generateGuestAccessToken(
      String guestIdentifier,
      String scopes,
      TenantConfig config,
      String clientId,
      String tenantId) {
    return tokenIssuer.generateAccessToken(
        "",
        getCurrentTimeInSeconds(),
        scopes,
        new JsonObject(Map.of(USERID, guestIdentifier)),
        Collections.emptyList(),
        clientId,
        tenantId,
        config);
  }

  public Single<RefreshTokenResponseDto> refreshTokens(
      V1RefreshTokenRequestDto dto, MultivaluedMap<String, String> headers, String tenantId) {
    TenantConfig config = registry.get(tenantId, TenantConfig.class);
    return refreshTokenDao
        .getUserIdFromRefreshToken(dto.getRefreshToken(), tenantId)
        .switchIfEmpty(Single.error(UNAUTHORIZED.getCustomException("Invalid refresh token")))
        .flatMap(
            userId -> {
              if (shouldSetAccessTokenAdditionalClaims(config)) {
                return userService
                    .getUser(Map.of(USERID, userId), headers, tenantId)
                    .map(
                        userResp ->
                            getAccessTokenClaims(
                                userResp,
                                getCurrentTimeInSeconds(),
                                config,
                                dto.getRefreshToken()));
              } else {
                return Single.just(
                    getAccessTokenClaims(
                        new JsonObject(Map.of(USERID, userId)),
                        getCurrentTimeInSeconds(),
                        config,
                        dto.getRefreshToken()));
              }
            })
        .flatMap(
            accessTokenClaims ->
                tokenIssuer.generateAccessToken(accessTokenClaims, config.getTenantId()))
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
            model ->
                generateTokens(
                    new JsonObject(model.getUser()),
                    String.join(" ", model.getScopes()),
                    model.getAuthMethods(),
                    model.getMetaInfo(),
                    model.getClientId(),
                    tenantId))
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

  public void revokeTokens(List<String> refreshTokens, String tenantId) {
    updateRevocations(refreshTokens, tenantId);
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

  public NewCookie getSsoTokenCookie(String ssoToken, String tenantId) {
    return buildCookie(
        SSO_TOKEN_COOKIE_NAME,
        ssoToken,
        registry.get(tenantId, TenantConfig.class).getTokenConfig().getRefreshTokenExpiry(),
        tenantId);
  }

  private Map<String, Object> getAccessTokenClaims(
      JsonObject userResponse, long iat, TenantConfig config, String refreshToken) {
    Map<String, Object> commonTokenClaims =
        getCommonTokenClaims(userResponse.getString(USERID), iat, config);
    Map<String, Object> accessTokenClaims = new HashMap<>(commonTokenClaims);
    accessTokenClaims.put(JWT_CLAIMS_RFT_ID, getRftId(refreshToken));
    accessTokenClaims.put(JWT_CLAIMS_EXP, iat + config.getTokenConfig().getAccessTokenExpiry());
    accessTokenClaims.put(JWT_TENANT_ID_CLAIM, config.getTenantId());
    return appendAdditionalAccessTokenClaims(accessTokenClaims, userResponse, config);
  }

  private Map<String, Object> getCommonTokenClaims(String userId, long iat, TenantConfig config) {
    Map<String, Object> commonTokenClaims = new HashMap<>();
    commonTokenClaims.put(JWT_CLAIMS_SUB, userId);
    commonTokenClaims.put(JWT_CLAIMS_IAT, iat);
    commonTokenClaims.put(JWT_CLAIMS_ISS, config.getTokenConfig().getIssuer());
    return commonTokenClaims;
  }
}

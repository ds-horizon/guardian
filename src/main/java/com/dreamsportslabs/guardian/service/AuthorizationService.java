package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.ACCESS_TOKEN_COOKIE_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.CODE;
import static com.dreamsportslabs.guardian.constant.Constants.IS_NEW_USER;
import static com.dreamsportslabs.guardian.constant.Constants.REFRESH_TOKEN_COOKIE_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.SSO_TOKEN_COOKIE_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.TOKEN;
import static com.dreamsportslabs.guardian.constant.Constants.TOKEN_TYPE;
import static com.dreamsportslabs.guardian.constant.Constants.USERID;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_CODE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.UNAUTHORIZED;
import static com.dreamsportslabs.guardian.utils.Utils.getCurrentTimeInSeconds;
import static com.dreamsportslabs.guardian.utils.Utils.getRftId;
import static com.dreamsportslabs.guardian.utils.Utils.shouldSetAccessTokenAdditionalClaims;

import com.dreamsportslabs.guardian.config.tenant.AuthCodeConfig;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.config.tenant.TokenConfig;
import com.dreamsportslabs.guardian.constant.AuthMethod;
import com.dreamsportslabs.guardian.dao.CodeDao;
import com.dreamsportslabs.guardian.dao.RefreshTokenDao;
import com.dreamsportslabs.guardian.dao.RevocationDao;
import com.dreamsportslabs.guardian.dao.model.CodeModel;
import com.dreamsportslabs.guardian.dao.model.RefreshTokenModel;
import com.dreamsportslabs.guardian.dao.model.SsoTokenModel;
import com.dreamsportslabs.guardian.dto.request.MetaInfo;
import com.dreamsportslabs.guardian.dto.request.V1CodeTokenExchangeRequestDto;
import com.dreamsportslabs.guardian.dto.request.V1LogoutRequestDto;
import com.dreamsportslabs.guardian.dto.request.v2.V2LogoutRequestDto;
import com.dreamsportslabs.guardian.dto.request.v2.V2RefreshTokenRequestDto;
import com.dreamsportslabs.guardian.dto.response.CodeResponseDto;
import com.dreamsportslabs.guardian.dto.response.IdpConnectResponseDto;
import com.dreamsportslabs.guardian.dto.response.MfaFactorDto;
import com.dreamsportslabs.guardian.dto.response.RefreshTokenResponseDto;
import com.dreamsportslabs.guardian.dto.response.TokenResponseDto;
import com.dreamsportslabs.guardian.registry.Registry;
import com.dreamsportslabs.guardian.utils.MfaFactorUtil;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class AuthorizationService {
  private final Registry registry;
  private final TokenIssuer tokenIssuer;

  private final RefreshTokenDao refreshTokenDao;
  private final CodeDao codeDao;
  private final RevocationDao revocationDao;
  private final ClientService clientService;
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
      return generateCode(user, scopes, authMethods, metaInfo, clientId, tenantId).map(res -> res);
    }
    throw INVALID_REQUEST.getException();
  }

  public Single<TokenResponseDto> generateMfaSignInTokens(
      JsonObject user,
      String refreshToken,
      List<String> scopes,
      List<AuthMethod> authMethods,
      String clientId,
      String tenantId) {
    TenantConfig config = registry.get(tenantId, TenantConfig.class);
    long iat = getCurrentTimeInSeconds();
    return Single.zip(
            tokenIssuer.generateAccessToken(
                refreshToken,
                iat,
                String.join(" ", scopes),
                user,
                authMethods,
                clientId,
                tenantId,
                config),
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
                    null,
                    TOKEN_TYPE,
                    config.getTokenConfig().getAccessTokenExpiry(),
                    user.getBoolean(IS_NEW_USER, false)))
        .flatMap(
            tokenResponseDto ->
                updateRefreshToken(refreshToken, authMethods, scopes, clientId, tenantId)
                    .andThen(Single.just(tokenResponseDto)));
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

    Single<Pair<String, List<String>>> clientMfaInfoSingle =
        clientService
            .getClient(clientId, tenantId)
            .map(
                client -> {
                  String mfaPolicy = client.getMfaPolicy();
                  List<String> allowedMfaMethods = client.getAllowedMfaMethods();
                  return Pair.of(
                      mfaPolicy != null ? mfaPolicy : "not_required",
                      allowedMfaMethods != null ? allowedMfaMethods : new ArrayList<String>());
                })
            .onErrorReturn(err -> Pair.of("not_required", new ArrayList<>()));

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
            clientMfaInfoSingle,
            (accessToken, idToken, clientMfaInfo) -> {
              String mfaPolicy = clientMfaInfo.getLeft();
              List<String> clientMfaMethods = clientMfaInfo.getRight();
              List<MfaFactorDto> mfaFactors = new ArrayList<>();
              if ("mandatory".equals(mfaPolicy)) {
                mfaFactors = MfaFactorUtil.buildMfaFactors(authMethods, user, clientMfaMethods);
              }
              return new TokenResponseDto(
                  accessToken,
                  refreshToken,
                  idToken,
                  ssoToken,
                  TOKEN_TYPE,
                  config.getTokenConfig().getAccessTokenExpiry(),
                  user.getBoolean(IS_NEW_USER, false),
                  mfaFactors);
            })
        .flatMap(
            dto ->
                refreshTokenDao
                    .saveRefreshToken(
                        getRefreshTokenDto(
                            refreshToken,
                            user,
                            iat,
                            Arrays.stream(StringUtils.split(scopes, " ")).toList(),
                            authMethods,
                            metaInfo,
                            clientId,
                            config),
                        getSsoTokenDto(
                            user.getString(USERID),
                            ssoToken,
                            iat,
                            refreshToken,
                            authMethods,
                            clientId,
                            config))
                    .andThen(Single.just(dto)));
  }

  private RefreshTokenModel getRefreshTokenDto(
      String refreshToken,
      JsonObject user,
      Long iat,
      List<String> scopes,
      List<AuthMethod> authMethods,
      MetaInfo metaInfo,
      String clientId,
      TenantConfig config) {
    return RefreshTokenModel.builder()
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
        .authMethod(authMethods)
        .build();
  }

  private SsoTokenModel getSsoTokenDto(
      String userId,
      String ssoToken,
      Long iat,
      String refreshToken,
      List<AuthMethod> authMethods,
      String clientId,
      TenantConfig config) {
    return SsoTokenModel.builder()
        .tenantId(config.getTenantId())
        .clientIdIssuedTo(clientId)
        .userId(userId)
        .refreshToken(refreshToken)
        .ssoToken(ssoToken)
        .expiry(iat + config.getTokenConfig().getRefreshTokenExpiry())
        .authMethods(authMethods)
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
      V2RefreshTokenRequestDto dto, MultivaluedMap<String, String> headers, String tenantId) {
    TenantConfig config = registry.get(tenantId, TenantConfig.class);
    if (StringUtils.isBlank(dto.getRefreshToken())) {
      return Single.error(UNAUTHORIZED.getCustomException("Invalid refresh token"));
    }
    return validateRefreshToken(tenantId, dto.getClientId(), dto.getRefreshToken())
        .flatMap(
            refreshTokenModel -> {
              if (shouldSetAccessTokenAdditionalClaims(config)) {
                return userService
                    .getUser(Map.of(USERID, refreshTokenModel.getUserId()), headers, tenantId)
                    .map(userResp -> Pair.of(userResp, refreshTokenModel));
              } else {
                return Single.just(new JsonObject(Map.of(USERID, refreshTokenModel.getUserId())))
                    .map(userResp -> Pair.of(userResp, refreshTokenModel));
              }
            })
        .flatMap(
            pair ->
                tokenIssuer.generateAccessToken(
                    dto.getRefreshToken(),
                    getCurrentTimeInSeconds(),
                    String.join(" ", pair.getRight().getScope()),
                    pair.getLeft(),
                    pair.getRight().getAuthMethod(),
                    pair.getRight().getClientId(),
                    tenantId,
                    config))
        .map(
            accessToken ->
                new RefreshTokenResponseDto(
                    accessToken, TOKEN_TYPE, config.getTokenConfig().getAccessTokenExpiry()));
  }

  private Single<CodeResponseDto> generateCode(
      JsonObject user,
      String scopes,
      List<AuthMethod> authMethods,
      MetaInfo metaInfo,
      String clientId,
      String tenantId) {
    AuthCodeConfig config = registry.get(tenantId, TenantConfig.class).getAuthCodeConfig();
    String code = RandomStringUtils.randomAlphanumeric(config.getLength());
    CodeModel codeModel =
        CodeModel.builder()
            .user(user.getMap())
            .code(code)
            .scopes(Arrays.stream(scopes.split("\\s+")).toList())
            .authMethods(authMethods)
            .metaInfo(metaInfo)
            .clientId(clientId)
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

  public Completable logout(V2LogoutRequestDto requestDto, String tenantId) {
    if (StringUtils.isBlank(requestDto.getRefreshToken())) {
      return Completable.error(UNAUTHORIZED.getCustomException("Invalid refresh token"));
    }
    return invalidateRefreshToken(requestDto, tenantId);
  }

  public Completable adminLogout(String userId, String tenantId) {
    return refreshTokenDao
        .getRefreshTokens(tenantId, userId)
        .flatMap(list -> refreshTokenDao.revokeTokens(tenantId, userId).andThen(Single.just(list)))
        .doOnSuccess(tokens -> updateRevocations(tokens, tenantId))
        .ignoreElement();
  }

  private Completable invalidateRefreshToken(V1LogoutRequestDto dto, String tenantId) {
    return refreshTokenDao
        .getRefreshToken(tenantId, dto.getRefreshToken())
        .switchIfEmpty(Single.error(UNAUTHORIZED.getCustomException("Invalid refresh token")))
        .flatMapCompletable(
            refreshTokenModel -> {
              if (dto.getIsUniversalLogout()) {
                return refreshTokenDao
                    .getRefreshTokens(tenantId, refreshTokenModel.getUserId())
                    .flatMap(
                        list ->
                            refreshTokenDao
                                .revokeTokens(tenantId, refreshTokenModel.getUserId())
                                .andThen(Single.just(list)))
                    .doOnSuccess(tokens -> updateRevocations(tokens, tenantId))
                    .ignoreElement();
              } else {
                return refreshTokenDao
                    .revokeToken(
                        tenantId,
                        refreshTokenModel.getClientId(),
                        refreshTokenModel.getRefreshToken())
                    .ignoreElement()
                    .doOnComplete(
                        () -> updateRevocations(List.of(dto.getRefreshToken()), tenantId));
              }
            });
  }

  public Single<RefreshTokenModel> validateRefreshToken(
      String tenantId, String clientId, String refreshToken) {
    return refreshTokenDao
        .getRefreshToken(tenantId, refreshToken)
        .switchIfEmpty(Single.error(UNAUTHORIZED.getCustomException("Invalid refresh token")))
        .filter(
            refreshTokenModel -> {
              if (StringUtils.isNotBlank(clientId)) {
                return refreshTokenModel.getClientId().equals(clientId);
              }
              return true;
            })
        .switchIfEmpty(Single.error(UNAUTHORIZED.getCustomException("Invalid refresh token")))
        .filter(
            refreshTokenModel ->
                refreshTokenModel.getRefreshTokenExp() > (getCurrentTimeInSeconds()))
        .switchIfEmpty(Single.error(UNAUTHORIZED.getCustomException("Invalid refresh token")))
        .filter(RefreshTokenModel::getIsActive)
        .switchIfEmpty(Single.error(UNAUTHORIZED.getCustomException("Invalid refresh token")));
  }

  public Completable updateRefreshToken(
      String refreshToken,
      List<AuthMethod> authMethods,
      List<String> scopes,
      String clientId,
      String tenantId) {
    return refreshTokenDao.updateRefreshToken(
        refreshToken, authMethods, scopes, clientId, tenantId);
  }

  private Completable invalidateRefreshToken(V2LogoutRequestDto dto, String tenantId) {
    return refreshTokenDao
        .getRefreshToken(tenantId, dto.getRefreshToken())
        .switchIfEmpty(Single.error(UNAUTHORIZED.getCustomException("Invalid refresh token")))
        .filter(
            refreshTokenModel -> {
              if (StringUtils.isNotBlank(dto.getClientId())) {
                return refreshTokenModel.getClientId().equals(dto.getClientId());
              }
              return true;
            })
        .switchIfEmpty(Single.error(UNAUTHORIZED.getCustomException("Invalid refresh token")))
        .filter(
            refreshTokenModel ->
                refreshTokenModel.getRefreshTokenExp() > (getCurrentTimeInSeconds()))
        .switchIfEmpty(Single.error(UNAUTHORIZED.getCustomException("Invalid refresh token")))
        .flatMapCompletable(
            refreshTokenModel -> {
              switch (dto.getLogoutType()) {
                case TOKEN -> {
                  return refreshTokenDao
                      .revokeToken(tenantId, refreshTokenModel.getClientId(), dto.getRefreshToken())
                      .doOnSuccess(
                          __ -> updateRevocations(List.of(dto.getRefreshToken()), tenantId))
                      .ignoreElement();
                }
                case CLIENT -> {
                  return refreshTokenDao
                      .getRefreshTokens(
                          tenantId, refreshTokenModel.getClientId(), refreshTokenModel.getUserId())
                      .flatMap(
                          list ->
                              refreshTokenDao
                                  .revokeTokens(
                                      tenantId,
                                      refreshTokenModel.getClientId(),
                                      refreshTokenModel.getUserId())
                                  .andThen(Single.just(list)))
                      .doOnSuccess(tokens -> updateRevocations(tokens, tenantId))
                      .ignoreElement();
                }
                case TENANT -> {
                  return clientService
                      .validateFirstPartyClient(tenantId, refreshTokenModel.getClientId())
                      .onErrorResumeNext(
                          err ->
                              Completable.error(
                                  UNAUTHORIZED.getCustomException("Invalid refresh token")))
                      .andThen(
                          refreshTokenDao.getRefreshTokens(tenantId, refreshTokenModel.getUserId()))
                      .flatMap(
                          list ->
                              refreshTokenDao
                                  .revokeTokens(tenantId, refreshTokenModel.getUserId())
                                  .andThen(Single.just(list)))
                      .doOnSuccess(tokens -> updateRevocations(tokens, tenantId))
                      .ignoreElement();
                }
              }
              return Completable.complete();
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
}

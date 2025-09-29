package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_AUD;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_CLIENT_ID;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_EXP;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_IAT;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_ISS;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_JTI;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_NONCE;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_RFT_ID;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_SCOPE;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_SUB;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_TENANT_ID_CLAIM;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_USERID;
import static com.dreamsportslabs.guardian.constant.Constants.TOKEN_TYPE;
import static com.dreamsportslabs.guardian.constant.Constants.USERID;
import static com.dreamsportslabs.guardian.constant.Constants.WWW_AUTHENTICATE_BASIC;
import static com.dreamsportslabs.guardian.constant.Constants.WWW_AUTHENTICATE_HEADER;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_CLIENT;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_GRANT;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_SCOPE;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.UNAUTHORIZED;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.UNAUTHORIZED_CLIENT;
import static com.dreamsportslabs.guardian.utils.Utils.getCurrentTimeInSeconds;
import static com.dreamsportslabs.guardian.utils.Utils.shouldSetAccessTokenAdditionalClaims;

import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.config.tenant.TokenConfig;
import com.dreamsportslabs.guardian.constant.OidcCodeChallengeMethod;
import com.dreamsportslabs.guardian.constant.OidcGrantType;
import com.dreamsportslabs.guardian.dao.OidcRefreshTokenDao;
import com.dreamsportslabs.guardian.dao.RefreshTokenDao;
import com.dreamsportslabs.guardian.dao.model.ClientModel;
import com.dreamsportslabs.guardian.dao.model.ClientScopeModel;
import com.dreamsportslabs.guardian.dao.model.OidcCodeModel;
import com.dreamsportslabs.guardian.dao.model.OidcRefreshTokenModel;
import com.dreamsportslabs.guardian.dao.model.ScopeModel;
import com.dreamsportslabs.guardian.dto.request.GenerateOidcTokenDto;
import com.dreamsportslabs.guardian.dto.request.RevokeTokenRequestDto;
import com.dreamsportslabs.guardian.dto.request.TokenRequestDto;
import com.dreamsportslabs.guardian.dto.request.scope.GetScopeRequestDto;
import com.dreamsportslabs.guardian.dto.response.OidcTokenResponseDto;
import com.dreamsportslabs.guardian.registry.Registry;
import com.dreamsportslabs.guardian.utils.Utils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class OidcTokenService {

  private final ClientService clientService;
  private final OidcCodeService oidcCodeService;
  private final ClientScopeService clientScopeService;
  private final ScopeService scopeService;
  private final UserService userService;
  private final TokenIssuer tokenIssuer;
  private final RefreshTokenDao refreshTokenDao;

  private final OidcRefreshTokenDao oidcRefreshTokenDao;
  private final AuthorizationService authorizationService;

  private final Registry registry;

  public Single<OidcTokenResponseDto> getOidcTokens(
      TokenRequestDto requestDto,
      String tenantId,
      String authorizationHeader,
      MultivaluedMap<String, String> headers) {
    return switch (requestDto.getOidcGrantType()) {
      case AUTHORIZATION_CODE -> authorizationCodeFlow(
          requestDto, tenantId, authorizationHeader, headers);
      case CLIENT_CREDENTIALS -> clientCredentialsFlow(requestDto, tenantId, authorizationHeader);
      case REFRESH_TOKEN -> refreshTokenFlow(requestDto, tenantId, authorizationHeader, headers);
    };
  }

  public Completable revokeOidcToken(
      RevokeTokenRequestDto requestDto, String tenantId, String authorizationHeader) {
    return authenticateClientUsingHeader(authorizationHeader, tenantId)
        .flatMap(
            clientModel ->
                oidcRefreshTokenDao.revokeOidcRefreshToken(
                    tenantId, clientModel.getClientId(), requestDto.getToken()))
        .flatMapCompletable(
            result -> {
              if (result) {
                authorizationService.revokeTokens(List.of(requestDto.getToken()), tenantId);
              }
              return Completable.complete();
            });
  }

  public Single<String> validateRefreshToken(String refreshToken, String tenantId) {
    return refreshTokenDao
        .getUserIdFromRefreshToken(refreshToken, tenantId)
        .onErrorResumeNext(
            err ->
                Maybe.error(
                    SERVER_ERROR.getJsonCustomException("Unable to validate refresh token")))
        .switchIfEmpty(Single.error(UNAUTHORIZED.getJsonCustomException("Invalid refresh token")));
  }

  private Single<OidcTokenResponseDto> authorizationCodeFlow(
      TokenRequestDto requestDto,
      String tenantId,
      String authorizationHeader,
      MultivaluedMap<String, String> headers) {

    return authenticateClient(requestDto, tenantId, authorizationHeader)
        .map(
            clientId -> {
              requestDto.setClientId(clientId);
              return clientId;
            })
        .flatMap(clientId -> validateCode(requestDto, tenantId))
        .flatMap(
            oidcCodeModel ->
                oidcCodeService
                    .deleteOidcCode(requestDto.getCode(), tenantId)
                    .toSingleDefault(oidcCodeModel))
        .flatMap(
            oidcCodeModel ->
                userService
                    .getOidcUser(getUserFilters(oidcCodeModel), headers, tenantId)
                    .map(
                        userResponse ->
                            getGenerateOidcTokenDto(
                                oidcCodeModel, tenantId, userResponse, requestDto)))
        .flatMap(
            generateOidcTokenDto ->
                generateOidcTokensForAuthorizationCodeFlow(generateOidcTokenDto)
                    .flatMap(
                        tokenResponseDto ->
                            oidcRefreshTokenDao
                                .saveOidcRefreshToken(
                                    getOidcRefreshTokenModel(
                                        tokenResponseDto, generateOidcTokenDto))
                                .toSingleDefault(tokenResponseDto)));
  }

  private Single<OidcTokenResponseDto> clientCredentialsFlow(
      TokenRequestDto requestDto, String tenantId, String authorizationHeader) {
    return authenticateClient(requestDto, tenantId, authorizationHeader)
        .map(
            clientId -> {
              requestDto.setClientId(clientId);
              return clientId;
            })
        .flatMap(
            clientId -> getAllowedScopes(requestDto.getClientId(), tenantId, requestDto.getScope()))
        .flatMap(
            allowedScopes ->
                generateOidcTokensForClientCredentialsFlow(
                    getGenerateOidcTokenDto(requestDto.getClientId(), allowedScopes, tenantId)));
  }

  private Single<OidcTokenResponseDto> refreshTokenFlow(
      TokenRequestDto requestDto,
      String tenantId,
      String authorizationHeader,
      MultivaluedMap<String, String> headers) {
    TenantConfig tenantConfig = registry.get(tenantId, TenantConfig.class);
    return authenticateClient(requestDto, tenantId, authorizationHeader)
        .map(
            clientId -> {
              requestDto.setClientId(clientId);
              return clientId;
            })
        .flatMap(clientId -> getOidcRefreshToken(requestDto, tenantId))
        .flatMap(
            oidcRefreshTokenModel -> {
              String scopes =
                  getValidScopes(
                      String.join(" ", oidcRefreshTokenModel.getScope()), requestDto.getScope());
              if (shouldSetAccessTokenAdditionalClaims(tenantConfig)) {
                return userService
                    .getOidcUser(
                        Map.of(USERID, oidcRefreshTokenModel.getUserId()), headers, tenantId)
                    .map(
                        oidcUserResponse ->
                            getGenerateOidcTokenDto(
                                requestDto.getClientId(), oidcUserResponse, tenantId, scopes));
              } else {
                return Single.just(
                    getGenerateOidcTokenDto(
                        requestDto.getClientId(),
                        JsonObject.of(OIDC_USERID, oidcRefreshTokenModel.getUserId()),
                        tenantId,
                        scopes));
              }
            })
        .flatMap(
            generateOidcTokenDto ->
                generateOidcTokensForRefreshTokenFlow(
                    generateOidcTokenDto, requestDto.getRefreshToken()));
  }

  private Single<String> authenticateClient(
      TokenRequestDto requestDto, String tenantId, String authorizationHeader) {
    Single<ClientModel> clientAuth;

    if (authorizationHeader != null) {
      clientAuth = authenticateClientUsingHeader(authorizationHeader, tenantId);
    } else {
      clientAuth =
          clientService.authenticateClient(
              requestDto.getClientId(), requestDto.getClientSecret(), tenantId);
    }

    return clientAuth
        .filter(clientModel -> validateClientGrantType(clientModel, requestDto.getGrantType()))
        .switchIfEmpty(Single.error(UNAUTHORIZED_CLIENT.getException()))
        .map(ClientModel::getClientId);
  }

  private Single<ClientModel> authenticateClientUsingHeader(
      String authorizationHeader, String tenantId) {
    return Single.fromCallable(() -> Utils.getCredentialsFromAuthHeader(authorizationHeader))
        .flatMap(
            credentials ->
                clientService.authenticateClient(credentials[0], credentials[1], tenantId))
        .onErrorResumeNext(err -> Single.error(createInvalidClientError(tenantId)));
  }

  private Single<OidcCodeModel> validateCode(TokenRequestDto requestDto, String tenantId) {
    return oidcCodeService
        .getOidcCode(requestDto.getCode(), tenantId)
        .filter(oidcCodeModel -> oidcCodeModel.getClientId().equals(requestDto.getClientId()))
        .switchIfEmpty(Single.error(INVALID_GRANT.getJsonCustomException("code is invalid")))
        .filter(oidcCodeModel -> oidcCodeModel.getRedirectUri().equals(requestDto.getRedirectUri()))
        .switchIfEmpty(
            Single.error(INVALID_GRANT.getJsonCustomException("redirect_uri is invalid")))
        .filter(
            oidcCodeModel ->
                validatePkceChallenge(
                    requestDto.getCodeVerifier(),
                    oidcCodeModel.getCodeChallenge(),
                    oidcCodeModel.getCodeChallengeMethod()))
        .switchIfEmpty(
            Single.error(INVALID_GRANT.getJsonCustomException("code_verifier is invalid")));
  }

  private Single<OidcRefreshTokenModel> getOidcRefreshToken(
      TokenRequestDto requestDto, String tenantId) {
    return oidcRefreshTokenDao
        .getOidcRefreshToken(tenantId, requestDto.getClientId(), requestDto.getRefreshToken())
        .switchIfEmpty(
            Single.error(INVALID_GRANT.getJsonCustomException("refresh_token is invalid")))
        .filter(
            oidcRefreshTokenModel ->
                oidcRefreshTokenModel.getRefreshTokenExp() > (getCurrentTimeInSeconds()))
        .switchIfEmpty(
            Single.error(INVALID_GRANT.getJsonCustomException("refresh_token is expired")));
  }

  private Single<String> getAllowedScopes(String clientId, String tenantId, String requestScopes) {
    return clientScopeService
        .getClientScopes(clientId, tenantId)
        .map(
            clientScopeModels -> {
              String allowedScopes =
                  clientScopeModels.stream()
                      .map(ClientScopeModel::getScope)
                      .collect(Collectors.joining(" "));

              return getValidScopes(allowedScopes, requestScopes);
            });
  }

  private String getValidScopes(String allowedScopes, String requestedScopes) {
    if (requestedScopes == null) {
      return allowedScopes;
    }
    List<String> requestedScopeList = List.of(requestedScopes.split(" "));
    List<String> allowedScopeList = List.of(allowedScopes.split(" "));
    for (String scope : requestedScopeList) {
      if (!allowedScopeList.contains(scope)) {
        throw INVALID_SCOPE.getException();
      }
    }
    return String.join(" ", requestedScopeList);
  }

  private Single<OidcTokenResponseDto> generateOidcTokensForAuthorizationCodeFlow(
      GenerateOidcTokenDto generateOidcTokenDto) {

    TenantConfig tenantConfig =
        registry.get(generateOidcTokenDto.getTenantId(), TenantConfig.class);
    TokenConfig tokenConfig = tenantConfig.getTokenConfig();
    String refreshToken = tokenIssuer.generateRefreshToken();
    generateOidcTokenDto.getUserResponse().put(USERID, generateOidcTokenDto.getUserId());
    return scopeService
        .getScopes(
            generateOidcTokenDto.getTenantId(), createScopeRequest(generateOidcTokenDto.getScope()))
        .map(
            scopeModels ->
                scopeModels.stream().map(ScopeModel::getClaims).flatMap(List::stream).toList())
        .flatMap(
            claims ->
                Single.zip(
                    tokenIssuer.generateAccessToken(
                        refreshToken,
                        generateOidcTokenDto.getIat(),
                        generateOidcTokenDto.getScope(),
                        generateOidcTokenDto.getUserResponse(),
                        generateOidcTokenDto.getAuthMethods(),
                        generateOidcTokenDto.getClientId(),
                        tenantConfig.getTenantId(),
                        tenantConfig),
                    tokenIssuer.generateIdToken(
                        generateOidcTokenDto.getIat(),
                        generateOidcTokenDto.getNonce(),
                        generateOidcTokenDto.getUserResponse(),
                        claims,
                        generateOidcTokenDto.getClientId(),
                        generateOidcTokenDto.getTenantId()),
                    (accessToken, idToken) ->
                        buildTokenResponse(
                            accessToken,
                            idToken,
                            refreshToken,
                            tokenConfig.getAccessTokenExpiry(),
                            String.join(" ", generateOidcTokenDto.getScope()))));
  }

  private GetScopeRequestDto createScopeRequest(String scope) {
    GetScopeRequestDto scopeRequest = new GetScopeRequestDto();
    scopeRequest.setNames(Arrays.asList(scope.trim().split("\\s+")));
    return scopeRequest;
  }

  private OidcTokenResponseDto buildTokenResponse(
      String accessToken, String idToken, String refreshToken, int expiresIn, String scope) {
    return OidcTokenResponseDto.builder()
        .accessToken(accessToken)
        .idToken(idToken)
        .refreshToken(refreshToken)
        .tokenType(TOKEN_TYPE)
        .expiresIn(expiresIn)
        .scope(scope)
        .build();
  }

  private Single<OidcTokenResponseDto> generateOidcTokensForClientCredentialsFlow(
      GenerateOidcTokenDto generateOidcTokenDto) {

    TenantConfig tenantConfig =
        registry.get(generateOidcTokenDto.getTenantId(), TenantConfig.class);
    TokenConfig tokenConfig = tenantConfig.getTokenConfig();

    return tokenIssuer
        .generateAccessToken(
            "",
            generateOidcTokenDto.getIat(),
            generateOidcTokenDto.getScope(),
            new JsonObject(Map.of(USERID, generateOidcTokenDto.getClientId())),
            generateOidcTokenDto.getAuthMethods(),
            generateOidcTokenDto.getClientId(),
            tenantConfig.getTenantId(),
            tenantConfig)
        .map(
            accessToken ->
                OidcTokenResponseDto.builder()
                    .accessToken(accessToken)
                    .tokenType(TOKEN_TYPE)
                    .expiresIn(tokenConfig.getAccessTokenExpiry())
                    .scope(String.join(" ", generateOidcTokenDto.getScope()))
                    .build());
  }

  private Single<OidcTokenResponseDto> generateOidcTokensForRefreshTokenFlow(
      GenerateOidcTokenDto generateOidcTokenDto, String refreshToken) {

    TenantConfig tenantConfig =
        registry.get(generateOidcTokenDto.getTenantId(), TenantConfig.class);

    TokenConfig tokenConfig = tenantConfig.getTokenConfig();
    generateOidcTokenDto.getUserResponse().put(USERID, generateOidcTokenDto.getUserId());
    return tokenIssuer
        .generateAccessToken(
            refreshToken,
            generateOidcTokenDto.getIat(),
            generateOidcTokenDto.getScope(),
            generateOidcTokenDto.getUserResponse(),
            generateOidcTokenDto.getAuthMethods(),
            generateOidcTokenDto.getClientId(),
            tenantConfig.getTenantId(),
            tenantConfig)
        .map(
            accessToken ->
                OidcTokenResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType(TOKEN_TYPE)
                    .expiresIn(tokenConfig.getAccessTokenExpiry())
                    .scope(String.join(" ", generateOidcTokenDto.getScope()))
                    .build());
  }

  private Boolean validatePkceChallenge(
      String codeVerifier, String codeChallenge, String codeChallengeMethod) {
    if (codeChallenge == null && codeChallengeMethod == null) return true;

    if (codeChallenge != null && codeChallengeMethod != null && codeVerifier == null) {
      throw INVALID_REQUEST.getJsonCustomException("code_verifier is required");
    }

    return switch (OidcCodeChallengeMethod.fromString(codeChallengeMethod)) {
      case PLAIN -> codeChallenge.equals(codeVerifier);
      case S256 -> codeChallenge.equals(hashAndEncodeString(codeVerifier));
      default -> false;
    };
  }

  private String hashAndEncodeString(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(StandardCharsets.US_ASCII));
      return new String(Base64.getUrlEncoder().withoutPadding().encode(hash));
    } catch (Exception e) {
      log.error("Error hashing and encoding code verifier string: {}", e.getMessage());
      throw INTERNAL_SERVER_ERROR.getException();
    }
  }

  private Boolean validateClientGrantType(ClientModel clientModel, String grantType) {
    return clientModel.getGrantTypes().contains(OidcGrantType.fromString(grantType));
  }

  private GenerateOidcTokenDto getGenerateOidcTokenDto(
      OidcCodeModel oidcCodeModel,
      String tenantId,
      JsonObject userResponse,
      TokenRequestDto tokenRequestDto) {
    return GenerateOidcTokenDto.builder()
        .clientId(oidcCodeModel.getClientId())
        .userId(oidcCodeModel.getUserId())
        .scope(oidcCodeModel.getScope())
        .userResponse(userResponse)
        .nonce(oidcCodeModel.getNonce())
        .tenantId(tenantId)
        .iat(getCurrentTimeInSeconds())
        .deviceName(tokenRequestDto.getDeviceName())
        .ip(tokenRequestDto.getIp())
        .build();
  }

  private GenerateOidcTokenDto getGenerateOidcTokenDto(
      String clientId, String allowedScopes, String tenantId) {
    return GenerateOidcTokenDto.builder()
        .clientId(clientId)
        .userId(clientId)
        .scope(allowedScopes)
        .tenantId(tenantId)
        .iat(getCurrentTimeInSeconds())
        .build();
  }

  private GenerateOidcTokenDto getGenerateOidcTokenDto(
      String clientId, JsonObject userResponse, String tenantId, String scopes) {
    return GenerateOidcTokenDto.builder()
        .clientId(clientId)
        .userId(userResponse.getString(OIDC_USERID))
        .tenantId(tenantId)
        .iat(getCurrentTimeInSeconds())
        .scope(scopes)
        .userResponse(userResponse)
        .build();
  }

  private OidcRefreshTokenModel getOidcRefreshTokenModel(
      OidcTokenResponseDto tokenResponseDto, GenerateOidcTokenDto generateOidcTokenDto) {

    TenantConfig tenantConfig =
        registry.get(generateOidcTokenDto.getTenantId(), TenantConfig.class);
    TokenConfig tokenConfig = tenantConfig.getTokenConfig();

    return OidcRefreshTokenModel.builder()
        .tenantId(generateOidcTokenDto.getTenantId())
        .clientId(generateOidcTokenDto.getClientId())
        .userId(generateOidcTokenDto.getUserId())
        .refreshToken(tokenResponseDto.getRefreshToken())
        .refreshTokenExp(generateOidcTokenDto.getIat() + tokenConfig.getRefreshTokenExpiry())
        .scope(Arrays.asList(generateOidcTokenDto.getScope().trim().split("\\s+")))
        .deviceName(generateOidcTokenDto.getDeviceName())
        .ip(generateOidcTokenDto.getIp())
        .build();
  }

  private Map<String, Object> getAccessTokenClaims(
      String aud,
      String clientId,
      long exp,
      long iat,
      String iss,
      String rftId,
      String scope,
      String sub,
      String tenantId) {
    Map<String, Object> accessTokenClaims = getCommonJwtClaims(aud, exp, iat, iss, sub);
    accessTokenClaims.put(JWT_CLAIMS_CLIENT_ID, clientId);
    accessTokenClaims.put(JWT_CLAIMS_JTI, RandomStringUtils.randomAlphanumeric(32));
    accessTokenClaims.put(JWT_CLAIMS_RFT_ID, rftId);
    accessTokenClaims.put(JWT_CLAIMS_SCOPE, scope);
    accessTokenClaims.put(JWT_TENANT_ID_CLAIM, tenantId);
    return accessTokenClaims;
  }

  private Map<String, Object> getIdTokenClaims(
      String aud, long exp, long iat, String iss, String nonce, String sub) {
    Map<String, Object> idTokenClaims = getCommonJwtClaims(aud, exp, iat, iss, sub);
    idTokenClaims.put(JWT_CLAIMS_NONCE, nonce);
    return idTokenClaims;
  }

  private Map<String, Object> getCommonJwtClaims(
      String aud, long exp, long iat, String iss, String sub) {
    Map<String, Object> commonClaims = new HashMap<>();
    commonClaims.put(JWT_CLAIMS_AUD, aud);
    commonClaims.put(JWT_CLAIMS_EXP, exp);
    commonClaims.put(JWT_CLAIMS_IAT, iat);
    commonClaims.put(JWT_CLAIMS_ISS, iss);
    commonClaims.put(JWT_CLAIMS_SUB, sub);
    return commonClaims;
  }

  private Map<String, String> getUserFilters(OidcCodeModel oidcCodeModel) {
    Map<String, String> filters = new HashMap<>();
    filters.put(USERID, oidcCodeModel.getUserId());
    return filters;
  }

  private WebApplicationException createInvalidClientError(String tenantId) {
    return INVALID_CLIENT
        .setHeaders(
            getFailedAuthenticationHeaders(
                registry.get(tenantId, TenantConfig.class).getTokenConfig().getIssuer()))
        .getJsonException();
  }

  private MultivaluedMap<String, Object> getFailedAuthenticationHeaders(String iss) {
    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    headers.add(WWW_AUTHENTICATE_HEADER, WWW_AUTHENTICATE_BASIC + "\"" + iss + "\"");
    return headers;
  }
}

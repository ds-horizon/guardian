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
import static com.dreamsportslabs.guardian.constant.Constants.SECONDS_TO_MILLISECONDS;
import static com.dreamsportslabs.guardian.constant.Constants.TOKEN_TYPE;
import static com.dreamsportslabs.guardian.constant.Constants.USERID;
import static com.dreamsportslabs.guardian.constant.Constants.WWW_AUTHENTICATE_BASIC;
import static com.dreamsportslabs.guardian.constant.Constants.WWW_AUTHENTICATE_HEADER;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_CLIENT;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_GRANT;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_SCOPE;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.UNAUTHORIZED_CLIENT;
import static com.dreamsportslabs.guardian.utils.Utils.getRftId;

import com.dreamsportslabs.guardian.config.tenant.OidcConfig;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.config.tenant.TokenConfig;
import com.dreamsportslabs.guardian.constant.OidcCodeChallengeMethod;
import com.dreamsportslabs.guardian.constant.OidcGrantType;
import com.dreamsportslabs.guardian.dao.OidcRefreshTokenDao;
import com.dreamsportslabs.guardian.dao.model.ClientModel;
import com.dreamsportslabs.guardian.dao.model.ClientScopeModel;
import com.dreamsportslabs.guardian.dao.model.OidcCodeModel;
import com.dreamsportslabs.guardian.dao.model.OidcRefreshTokenModel;
import com.dreamsportslabs.guardian.dao.model.ScopeModel;
import com.dreamsportslabs.guardian.dto.request.GenerateOidcTokenDto;
import com.dreamsportslabs.guardian.dto.request.TokenRequestDto;
import com.dreamsportslabs.guardian.dto.request.scope.GetScopeRequestDto;
import com.dreamsportslabs.guardian.dto.response.OidcTokenResponseDto;
import com.dreamsportslabs.guardian.exception.OidcErrorEnum;
import com.dreamsportslabs.guardian.registry.Registry;
import com.dreamsportslabs.guardian.utils.Utils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  private final OidcRefreshTokenDao oidcRefreshTokenDao;

  private final Registry registry;

  public Single<OidcTokenResponseDto> getOidcTokens(
      TokenRequestDto requestDto,
      String tenantId,
      String authorizationHeader,
      MultivaluedMap<String, String> headers) {
    return switch (requestDto.getOidcGrantType()) {
      case AUTHORIZATION_CODE -> authorizationCodeFlow(
          requestDto, tenantId, authorizationHeader, headers);
      case CLIENT_CREDENTIALS -> clientCredentialsFlow(
          requestDto, tenantId, authorizationHeader, headers);
      case REFRESH_TOKEN -> refreshTokenFlow(requestDto, tenantId, authorizationHeader, headers);
      default -> Single.error(INVALID_GRANT.getException());
    };
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
                    .andThen(Single.just(oidcCodeModel)))
        .flatMap(
            oidcCodeModel ->
                userService
                    .getOidcUser(getUserFilters(oidcCodeModel), headers, tenantId)
                    .flatMap(
                        userResponse -> {
                          GenerateOidcTokenDto generateOidcTokenDto =
                              getGenerateOidcTokenDto(oidcCodeModel, tenantId, userResponse);
                          return Single.just(generateOidcTokenDto);
                        }))
        .flatMap(
            generateOidcTokenDto ->
                generateOidcTokensForAuthorizationCodeFlow(generateOidcTokenDto)
                    .flatMap(
                        tokenResponseDto -> {
                          OidcRefreshTokenModel refreshTokenModel =
                              getOidcRefreshTokenModel(tokenResponseDto, generateOidcTokenDto);
                          return oidcRefreshTokenDao
                              .saveOidcRefreshToken(refreshTokenModel)
                              .andThen(Single.just(tokenResponseDto));
                        }));
  }

  private Single<OidcTokenResponseDto> clientCredentialsFlow(
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
    return authenticateClient(requestDto, tenantId, authorizationHeader)
        .map(
            clientId -> {
              requestDto.setClientId(clientId);
              return clientId;
            })
        .flatMap(clientId -> validateRefreshToken(requestDto, tenantId))
        .flatMap(
            oidcRefreshTokenModel -> {
              List<String> scopes =
                  getValidScopes(oidcRefreshTokenModel.getScope(), requestDto.getScope());
              GenerateOidcTokenDto generateOidcTokenDto =
                  getGenerateOidcTokenDto(
                      requestDto.getClientId(),
                      oidcRefreshTokenModel.getUserId(),
                      tenantId,
                      scopes);
              return generateOidcTokensForRefreshTokenFlow(
                  generateOidcTokenDto, requestDto.getRefreshToken());
            });
  }

  private Single<String> authenticateClient(
      TokenRequestDto requestDto, String tenantId, String authorizationHeader) {
    if (authorizationHeader != null) {
      return Single.just(Utils.getCredentialsFromAuthHeader(authorizationHeader))
          .flatMap(
              clientCredentials ->
                  clientService.authenticateClient(
                      clientCredentials[0], clientCredentials[1], tenantId))
          .onErrorResumeNext(
              err ->
                  Single.error(
                      INVALID_CLIENT
                          .setHeaders(
                              getFailedAuthenticationHeaders(
                                  registry
                                      .get(tenantId, TenantConfig.class)
                                      .getOidcConfig()
                                      .getIssuer()))
                          .getJsonException()))
          .filter(clientModel -> validateClientGrantType(clientModel, requestDto.getGrantType()))
          .switchIfEmpty(Single.error(UNAUTHORIZED_CLIENT.getException()))
          .map(ClientModel::getClientId);
    } else {
      return clientService
          .authenticateClient(requestDto.getClientId(), requestDto.getClientSecret(), tenantId)
          .filter(clientModel -> validateClientGrantType(clientModel, requestDto.getGrantType()))
          .switchIfEmpty(Single.error(UNAUTHORIZED_CLIENT.getException()))
          .map(ClientModel::getClientId);
    }
  }

  private Single<OidcCodeModel> validateCode(TokenRequestDto requestDto, String tenantId) {
    return oidcCodeService
        .getOidcCode(requestDto.getCode(), tenantId)
        .onErrorResumeNext(
            err ->
                Single.error(
                    INVALID_GRANT.getJsonCustomException("The authorization_code is invalid")))
        .filter(
            oidcCodeModel ->
                oidcCodeModel.getClient().getClientId().equals(requestDto.getClientId()))
        .switchIfEmpty(
            Single.error(INVALID_GRANT.getJsonCustomException("The authorization_code is invalid")))
        .filter(oidcCodeModel -> oidcCodeModel.getRedirectUri().equals(requestDto.getRedirectUri()))
        .switchIfEmpty(
            Single.error(INVALID_GRANT.getJsonCustomException("The redirect_uri is invalid")))
        .filter(
            oidcCodeModel ->
                validatePkceChallenge(
                    requestDto.getCodeVerifier(),
                    oidcCodeModel.getCodeChallenge(),
                    oidcCodeModel.getCodeChallengeMethod()))
        .switchIfEmpty(
            Single.error(INVALID_GRANT.getJsonCustomException("The code_verifier is invalid")));
  }

  private Single<OidcRefreshTokenModel> validateRefreshToken(
      TokenRequestDto requestDto, String tenantId) {
    return oidcRefreshTokenDao
        .getOidcRefreshToken(tenantId, requestDto.getClientId(), requestDto.getRefreshToken())
        .switchIfEmpty(
            Single.error(INVALID_GRANT.getJsonCustomException("The refresh_token is invalid")))
        .filter(OidcRefreshTokenModel::getIsActive)
        .switchIfEmpty(
            Single.error(INVALID_GRANT.getJsonCustomException("The refresh_token is inactive")))
        .filter(
            oidcRefreshTokenModel ->
                oidcRefreshTokenModel.getRefreshTokenExp()
                    > (System.currentTimeMillis() / SECONDS_TO_MILLISECONDS))
        .switchIfEmpty(
            Single.error(INVALID_GRANT.getJsonCustomException("The refresh_token is expired")))
        .map(oidcRefreshTokenModel -> oidcRefreshTokenModel);
  }

  private Single<List<String>> getAllowedScopes(
      String clientId, String tenantId, String requestScopes) {
    return clientScopeService
        .getClientScopes(clientId, tenantId)
        .map(
            clientScopeModels -> {
              List<String> allowedScopes =
                  clientScopeModels.stream().map(ClientScopeModel::getScope).toList();
              return getValidScopes(allowedScopes, requestScopes);
            });
  }

  private List<String> getValidScopes(List<String> allowedScopes, String requestScopes) {
    if (requestScopes == null) {
      return allowedScopes;
    }
    List<String> requestedScopes = List.of(requestScopes.split(" "));
    for (String scope : requestedScopes) {
      if (!allowedScopes.contains(scope)) {
        throw INVALID_SCOPE.getException();
      }
    }
    return requestedScopes;
  }

  private Single<OidcTokenResponseDto> generateOidcTokensForAuthorizationCodeFlow(
      GenerateOidcTokenDto generateOidcTokenDto) {
    TenantConfig tenantConfig =
        registry.get(generateOidcTokenDto.getTenantId(), TenantConfig.class);
    TokenConfig tokenConfig = tenantConfig.getTokenConfig();
    OidcConfig oidcConfig = tenantConfig.getOidcConfig();
    String refreshToken = tokenIssuer.generateRefreshToken();
    Map<String, Object> accessTokenClaims =
        getAccessTokenClaims(
            generateOidcTokenDto.getClientId(),
            generateOidcTokenDto.getClientId(),
            generateOidcTokenDto.getIat() + tokenConfig.getAccessTokenExpiry(),
            generateOidcTokenDto.getIat(),
            oidcConfig.getIssuer(),
            getRftId(refreshToken),
            generateOidcTokenDto.getScope(),
            generateOidcTokenDto.getUserId());
    Map<String, Object> idTokenClaims =
        getIdTokenClaims(
            generateOidcTokenDto.getClientId(),
            generateOidcTokenDto.getIat() + tokenConfig.getIdTokenExpiry(),
            generateOidcTokenDto.getIat(),
            oidcConfig.getIssuer(),
            generateOidcTokenDto.getNonce(),
            generateOidcTokenDto.getUserId());
    GetScopeRequestDto getScopeRequestDto = new GetScopeRequestDto();
    getScopeRequestDto.setNames(generateOidcTokenDto.getScope());
    return scopeService
        .getScopes(generateOidcTokenDto.getTenantId(), getScopeRequestDto)
        .map(
            scopeModels ->
                scopeModels.stream().map(ScopeModel::getClaims).flatMap(List::stream).toList())
        .flatMap(
            claims ->
                Single.zip(
                    tokenIssuer.generateAccessToken(
                        accessTokenClaims, generateOidcTokenDto.getTenantId()),
                    tokenIssuer.generateIdToken(
                        idTokenClaims,
                        generateOidcTokenDto.getUserResponse(),
                        generateOidcTokenDto.getTenantId(),
                        claims),
                    (accessToken, idToken) ->
                        OidcTokenResponseDto.builder()
                            .accessToken(accessToken)
                            .idToken(idToken)
                            .refreshToken(refreshToken)
                            .tokenType(TOKEN_TYPE)
                            .expiresIn(tokenConfig.getAccessTokenExpiry())
                            .build()));
  }

  private Single<OidcTokenResponseDto> generateOidcTokensForClientCredentialsFlow(
      GenerateOidcTokenDto generateOidcTokenDto) {
    TenantConfig tenantConfig =
        registry.get(generateOidcTokenDto.getTenantId(), TenantConfig.class);
    TokenConfig tokenConfig = tenantConfig.getTokenConfig();
    OidcConfig oidcConfig = tenantConfig.getOidcConfig();
    Map<String, Object> accessTokenClaims =
        getAccessTokenClaims(
            generateOidcTokenDto.getClientId(),
            generateOidcTokenDto.getClientId(),
            generateOidcTokenDto.getIat() + tokenConfig.getAccessTokenExpiry(),
            generateOidcTokenDto.getIat(),
            oidcConfig.getIssuer(),
            null,
            generateOidcTokenDto.getScope(),
            generateOidcTokenDto.getUserId());
    return tokenIssuer
        .generateAccessToken(accessTokenClaims, generateOidcTokenDto.getTenantId())
        .map(
            accessToken ->
                OidcTokenResponseDto.builder()
                    .accessToken(accessToken)
                    .tokenType(TOKEN_TYPE)
                    .expiresIn(tokenConfig.getAccessTokenExpiry())
                    .build());
  }

  private Single<OidcTokenResponseDto> generateOidcTokensForRefreshTokenFlow(
      GenerateOidcTokenDto generateOidcTokenDto, String refreshToken) {
    TenantConfig tenantConfig =
        registry.get(generateOidcTokenDto.getTenantId(), TenantConfig.class);
    TokenConfig tokenConfig = tenantConfig.getTokenConfig();
    OidcConfig oidcConfig = tenantConfig.getOidcConfig();
    Map<String, Object> accessTokenClaims =
        getAccessTokenClaims(
            generateOidcTokenDto.getClientId(),
            generateOidcTokenDto.getClientId(),
            generateOidcTokenDto.getIat() + tokenConfig.getAccessTokenExpiry(),
            generateOidcTokenDto.getIat(),
            oidcConfig.getIssuer(),
            getRftId(refreshToken),
            generateOidcTokenDto.getScope(),
            generateOidcTokenDto.getUserId());
    return tokenIssuer
        .generateAccessToken(accessTokenClaims, generateOidcTokenDto.getTenantId())
        .map(
            accessToken ->
                OidcTokenResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType(TOKEN_TYPE)
                    .expiresIn(tokenConfig.getAccessTokenExpiry())
                    .build());
  }

  private Boolean validatePkceChallenge(
      String codeVerifier, String codeChallenge, OidcCodeChallengeMethod codeChallengeMethod) {
    if (codeChallenge == null && codeChallengeMethod == null) return true;
    if (codeChallenge != null && codeChallengeMethod != null && codeVerifier == null)
      throw INVALID_REQUEST.getJsonCustomException("code_verifier is required");
    if (codeChallengeMethod == null) {
      throw OidcErrorEnum.INTERNAL_SERVER_ERROR.getException();
    }
    switch (codeChallengeMethod) {
      case PLAIN -> {
        return codeChallenge.equals(codeVerifier);
      }
      case S256 -> {
        return codeChallenge.equals(hashAndEncodeString(codeVerifier));
      }
      default -> {
        return false;
      }
    }
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
      OidcCodeModel oidcCodeModel, String tenantId, JsonObject userResponse) {
    return GenerateOidcTokenDto.builder()
        .clientId(oidcCodeModel.getClient().getClientId())
        .userId(oidcCodeModel.getUserId())
        .scope(oidcCodeModel.getConsentedScopes())
        .userResponse(userResponse)
        .nonce(oidcCodeModel.getNonce())
        .tenantId(tenantId)
        .iat(System.currentTimeMillis() / SECONDS_TO_MILLISECONDS)
        .build();
  }

  private GenerateOidcTokenDto getGenerateOidcTokenDto(
      String clientId, List<String> allowedScopes, String tenantId) {
    return GenerateOidcTokenDto.builder()
        .clientId(clientId)
        .userId(clientId)
        .scope(allowedScopes)
        .tenantId(tenantId)
        .iat(System.currentTimeMillis() / SECONDS_TO_MILLISECONDS)
        .build();
  }

  private GenerateOidcTokenDto getGenerateOidcTokenDto(
      String clientId, String userId, String tenantId, List<String> scopes) {
    return GenerateOidcTokenDto.builder()
        .clientId(clientId)
        .userId(userId)
        .tenantId(tenantId)
        .iat(System.currentTimeMillis() / SECONDS_TO_MILLISECONDS)
        .scope(scopes)
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
        .scope(generateOidcTokenDto.getScope())
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
      List<String> scope,
      String sub) {
    Map<String, Object> accessTokenClaims = getCommonJwtClaims(aud, exp, iat, iss, sub);
    accessTokenClaims.put(JWT_CLAIMS_CLIENT_ID, clientId);
    accessTokenClaims.put(JWT_CLAIMS_JTI, RandomStringUtils.randomAlphanumeric(32));
    accessTokenClaims.put(JWT_CLAIMS_RFT_ID, rftId);
    accessTokenClaims.put(JWT_CLAIMS_SCOPE, String.join(" ", scope));
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

  private MultivaluedMap<String, Object> getFailedAuthenticationHeaders(String iss) {
    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    headers.add(WWW_AUTHENTICATE_HEADER, WWW_AUTHENTICATE_BASIC + "\"" + iss + "\"");
    return headers;
  }
}

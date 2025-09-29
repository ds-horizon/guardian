package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.AUTHORIZATION;
import static com.dreamsportslabs.guardian.constant.Constants.ERROR;
import static com.dreamsportslabs.guardian.constant.Constants.ERROR_DESCRIPTION;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_SUB;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_AUTHORIZATION_CODE;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_CLAIMS_EMAIL;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_CLAIMS_FAMILY_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_CLAIMS_FULL_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_CLAIMS_GIVEN_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_CLAIMS_PHONE;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_CLIENT_ID;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_CLIENT_SECRET;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_CODE;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_CODE_VERIFIER;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_GRANT_TYPE;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_NONCE;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_REDIRECT_URI;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_TOKENS_ACCESS_TOKEN;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_TOKENS_ID_TOKEN;
import static com.dreamsportslabs.guardian.constant.Constants.USERID;
import static com.dreamsportslabs.guardian.constant.Constants.USER_FILTERS_EMAIL;
import static com.dreamsportslabs.guardian.constant.Constants.USER_FILTERS_PHONE;
import static com.dreamsportslabs.guardian.constant.Constants.USER_FILTERS_PROVIDER_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.USER_FILTERS_PROVIDER_USER_ID;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_IDP_CODE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_IDP_TOKEN;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.PROVIDER_TOKENS_EXCHANGE_FAILED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.USER_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.USER_NOT_EXISTS;

import com.dreamsportslabs.guardian.config.tenant.OidcProviderConfig;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.constant.BlockFlow;
import com.dreamsportslabs.guardian.constant.ClientAuthMethod;
import com.dreamsportslabs.guardian.constant.Flow;
import com.dreamsportslabs.guardian.constant.IdpUserIdentifier;
import com.dreamsportslabs.guardian.dao.model.IdpCredentials;
import com.dreamsportslabs.guardian.dto.Provider;
import com.dreamsportslabs.guardian.dto.UserDto;
import com.dreamsportslabs.guardian.dto.request.IdpConnectRequestDto;
import com.dreamsportslabs.guardian.dto.response.IdpConnectResponseDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.jwtVerifier.TokenVerifier;
import com.dreamsportslabs.guardian.registry.Registry;
import com.dreamsportslabs.guardian.utils.Utils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.MultiMap;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.client.HttpRequest;
import io.vertx.rxjava3.ext.web.client.WebClient;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class IdpConnectService {
  private final UserService userService;
  private final AuthorizationService authorizationService;
  private final WebClient webClient;
  private final Registry registry;
  private final UserFlowBlockService userFlowBlockService;

  public Single<IdpConnectResponseDto> connect(
      IdpConnectRequestDto requestDto, MultivaluedMap<String, String> headers, String tenantId) {

    TenantConfig tenantConfig = registry.get(tenantId, TenantConfig.class);
    String providerName = requestDto.getIdProvider();
    OidcProviderConfig oidcProviderConfig = tenantConfig.getOidcProviderConfig().get(providerName);

    if (oidcProviderConfig == null) {
      return Single.error(
          ErrorEnum.INVALID_REQUEST.getCustomException(
              "Specified provider is not configured for tenant"));
    }

    String userIdentifier = oidcProviderConfig.getUserIdentifier();

    return verifyIdentifierAndGetProviderTokens(requestDto, oidcProviderConfig)
        .map(
            idpTokens -> {
              Provider provider = createProviderFromTokens(idpTokens, providerName);
              UserDto userDto = createUserDtoFromTokens(provider);
              return Pair.of(idpTokens, userDto);
            })
        .flatMap(
            pair -> {
              IdpCredentials idpTokens = pair.getLeft();
              UserDto userDto = pair.getRight();

              // TODO: Implement block for phone Number and provider UserId. Need to think how
              // tenant will get provider userId.
              String email = userDto.getEmail();
              if (email != null) {
                return userFlowBlockService
                    .isFlowBlocked(tenantId, List.of(email), BlockFlow.SOCIAL_AUTH)
                    .andThen(Single.just(Pair.of(idpTokens, userDto)));
              }
              return Single.just(Pair.of(idpTokens, userDto));
            })
        .flatMap(
            pair -> {
              IdpCredentials idpTokens = pair.getLeft();
              UserDto userDto = pair.getRight();

              Map<String, String> queryParams =
                  getUserIdentifierDetails(userIdentifier, userDto, requestDto.getIdProvider());
              return processUserBasedOnFlow(
                      queryParams, userDto, requestDto.getLoginFlow(), headers, tenantId)
                  .flatMap(
                      userJson ->
                          authorizationService
                              .generate(
                                  userJson,
                                  requestDto.getIdpResponseType().getResponseType(),
                                  requestDto.getMetaInfo(),
                                  tenantId)
                              .map(
                                  responseDto -> {
                                    Boolean isNewUser =
                                        (Boolean) userJson.getValue("isNewUser", false);
                                    return IdpConnectResponseDto.buildIdpConnectResponse(
                                        responseDto, isNewUser, idpTokens);
                                  }));
            });
  }

  private Provider createProviderFromTokens(IdpCredentials idpTokens, String providerName) {
    Map<String, Object> claims = decodeJwtPayload(idpTokens.getIdToken());
    Object userIdClaim = claims.get(JWT_CLAIMS_SUB);
    String providerUserId = userIdClaim != null ? userIdClaim.toString() : null;

    Map<String, Object> credentials = new HashMap<>();
    credentials.put(OIDC_TOKENS_ACCESS_TOKEN, idpTokens.getAccessToken());
    credentials.put(OIDC_REFRESH_TOKEN, idpTokens.getRefreshToken());
    credentials.put(OIDC_TOKENS_ID_TOKEN, idpTokens.getIdToken());

    return Provider.builder()
        .name(providerName)
        .data(claims)
        .providerUserId(providerUserId)
        .credentials(credentials)
        .build();
  }

  private Map<String, Object> decodeJwtPayload(String jwt) {
    try {
      String[] parts = jwt.split("\\.");
      if (parts.length != 3) {
        throw INVALID_IDP_TOKEN.getCustomException("Invalid JWT format");
      }
      String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
      return new JsonObject(payloadJson).getMap();
    } catch (Exception e) {
      throw INTERNAL_SERVER_ERROR.getCustomException("Failed to decode JWT payload");
    }
  }

  private UserDto createUserDtoFromTokens(Provider provider) {
    Map<String, Object> claims = provider.getData();

    UserDto.UserDtoBuilder userDtoBuilder = UserDto.builder();
    if (claims.containsKey(OIDC_CLAIMS_GIVEN_NAME)) {
      userDtoBuilder.firstName(claims.get(OIDC_CLAIMS_GIVEN_NAME).toString());
    }
    if (claims.containsKey(OIDC_CLAIMS_FAMILY_NAME)) {
      userDtoBuilder.lastName(claims.get(OIDC_CLAIMS_FAMILY_NAME).toString());
    }
    if (claims.containsKey(OIDC_CLAIMS_FULL_NAME)) {
      userDtoBuilder.name(claims.get(OIDC_CLAIMS_FULL_NAME).toString());
    }
    if (claims.containsKey(OIDC_CLAIMS_EMAIL)) {
      userDtoBuilder.email(claims.get(OIDC_CLAIMS_EMAIL).toString());
    }
    if (claims.containsKey(OIDC_CLAIMS_PHONE)) {
      userDtoBuilder.phoneNumber(claims.get(OIDC_CLAIMS_PHONE).toString());
    }
    userDtoBuilder.provider(provider);

    return userDtoBuilder.build();
  }

  private Map<String, String> getUserIdentifierDetails(
      String userIdentifier, UserDto userDto, String idProvider) {

    Map<String, String> queryParams = new HashMap<>();
    boolean isUserIdentifierExists;

    IdpUserIdentifier identifierType = IdpUserIdentifier.fromString(userIdentifier);

    switch (identifierType) {
      case PHONE:
        isUserIdentifierExists = StringUtils.isNotBlank(userDto.getPhoneNumber());
        queryParams.put(USER_FILTERS_PHONE, userDto.getPhoneNumber());
        break;
      case SUB:
        isUserIdentifierExists = StringUtils.isNotBlank(userDto.getProvider().getProviderUserId());
        queryParams.put(USER_FILTERS_PROVIDER_USER_ID, userDto.getProvider().getProviderUserId());
        queryParams.put(USER_FILTERS_PROVIDER_NAME, idProvider);
        break;
      default:
        isUserIdentifierExists = StringUtils.isNotBlank(userDto.getEmail());
        queryParams.put(USER_FILTERS_EMAIL, userDto.getEmail());
    }

    if (!isUserIdentifierExists) {
      throw ErrorEnum.INVALID_USER_IDENTIFIER.getException();
    }

    return queryParams;
  }

  private Single<JsonObject> processUserBasedOnFlow(
      Map<String, String> queryParams,
      UserDto userDto,
      Flow flow,
      MultivaluedMap<String, String> headers,
      String tenantId) {

    return userService
        .getUser(queryParams, headers, tenantId)
        .flatMap(
            user -> {
              boolean userExists = user.getString(USERID) != null;
              boolean requireProviderEndpoint =
                  registry
                      .get(tenantId, TenantConfig.class)
                      .getUserConfig()
                      .getIsProviderEndpointRequired();

              switch (flow) {
                case SIGNIN:
                  if (!userExists) {
                    return Single.error(USER_NOT_EXISTS.getCustomException("User does not exist"));
                  }
                  if (requireProviderEndpoint) {
                    return userService
                        .addProvider(
                            user.getString(USERID), headers, userDto.getProvider(), tenantId)
                        .andThen(Single.just(user));
                  } else {
                    return Single.just(user);
                  }

                case SIGNUP:
                  if (userExists) {
                    return Single.error(USER_EXISTS.getCustomException("User already exists"));
                  }
                  return userService.createUser(userDto, headers, tenantId);

                case SIGNINUP:
                  if (userExists) {
                    if (requireProviderEndpoint) {
                      return userService
                          .addProvider(
                              user.getString(USERID), headers, userDto.getProvider(), tenantId)
                          .andThen(Single.just(user));
                    } else {
                      return Single.just(user);
                    }
                  } else {
                    return userService.createUser(userDto, headers, tenantId);
                  }

                default:
                  if (requireProviderEndpoint) {
                    return userService
                        .addProvider(
                            user.getString(USERID), headers, userDto.getProvider(), tenantId)
                        .andThen(Single.just(user));
                  } else {
                    return Single.just(user);
                  }
              }
            });
  }

  private Single<IdpCredentials> verifyIdentifierAndGetProviderTokens(
      IdpConnectRequestDto idpConnectRequestDto, OidcProviderConfig oidcProviderConfig) {

    switch (idpConnectRequestDto.getOidcIdentifierType()) {
      case ID_TOKEN:
        return verifyIdToken(idpConnectRequestDto, oidcProviderConfig);
      case CODE:
        return exchangeCodeForTokens(idpConnectRequestDto, oidcProviderConfig);
      default:
        return Single.error(ErrorEnum.INVALID_IDENTIFIER_TYPE.getException());
    }
  }

  private Single<IdpCredentials> verifyIdToken(
      IdpConnectRequestDto idpConnectRequestDto, OidcProviderConfig oidcProviderConfig) {

    try {
      TokenVerifier tokenVerifier =
          new TokenVerifier(oidcProviderConfig.getJwksUrl(), oidcProviderConfig.getIssuer());
      Map<String, Object> claims =
          tokenVerifier.verify(
              idpConnectRequestDto.getIdentifier(), oidcProviderConfig.getClientId());

      verifyNonceClaim(idpConnectRequestDto, claims);
      return Single.just(
          IdpCredentials.builder().idToken(idpConnectRequestDto.getIdentifier()).build());

    } catch (Exception e) {
      throw INVALID_IDP_TOKEN.getException();
    }
  }

  private void verifyNonceClaim(
      IdpConnectRequestDto idpConnectRequestDto, Map<String, Object> claims) {

    String requestNonce = idpConnectRequestDto.getNonce();
    if (StringUtils.isNotBlank(requestNonce)) {
      Object nonceClaim = claims.get(OIDC_NONCE);
      if (nonceClaim == null || !requestNonce.equals(nonceClaim.toString())) {
        throw INVALID_IDP_TOKEN.getException();
      }
    }
  }

  public Single<IdpCredentials> exchangeCodeForTokens(
      IdpConnectRequestDto requestDto, OidcProviderConfig oidcProviderConfig) {

    MultiMap oidcTokenRequestBody = buildTokenExchangeRequest(requestDto, oidcProviderConfig);

    HttpRequest<Buffer> httpRequest =
        webClient
            .postAbs(oidcProviderConfig.getTokenUrl())
            .ssl(oidcProviderConfig.getIsSslEnabled());

    if (oidcProviderConfig
        .getClientAuthMethod()
        .getValue()
        .equals(ClientAuthMethod.BASIC.getValue())) {
      httpRequest =
          httpRequest.putHeader(
              AUTHORIZATION,
              Utils.generateBasicAuthHeader(
                  oidcProviderConfig.getClientId(), oidcProviderConfig.getClientSecret()));
    }

    return initiateTokenExchange(httpRequest, oidcTokenRequestBody);
  }

  public MultiMap buildTokenExchangeRequest(
      IdpConnectRequestDto requestDto, OidcProviderConfig oidcProviderConfig) {
    MultiMap oidcTokenRequestBody = MultiMap.caseInsensitiveMultiMap();
    oidcTokenRequestBody.add(OIDC_GRANT_TYPE, OIDC_AUTHORIZATION_CODE);
    oidcTokenRequestBody.add(OIDC_CODE, requestDto.getIdentifier());
    oidcTokenRequestBody.add(OIDC_REDIRECT_URI, oidcProviderConfig.getRedirectUri());
    if (StringUtils.isNotBlank(requestDto.getCodeVerifier())) {
      oidcTokenRequestBody.add(OIDC_CODE_VERIFIER, requestDto.getCodeVerifier());
    }
    if (oidcProviderConfig
        .getClientAuthMethod()
        .getValue()
        .equals(ClientAuthMethod.POST.getValue())) {
      oidcTokenRequestBody.add(OIDC_CLIENT_ID, oidcProviderConfig.getClientId());
      oidcTokenRequestBody.add(OIDC_CLIENT_SECRET, oidcProviderConfig.getClientSecret());
    }
    return oidcTokenRequestBody;
  }

  public Single<IdpCredentials> initiateTokenExchange(
      HttpRequest<Buffer> httpRequest, MultiMap oidcTokenRequestBody) {
    return httpRequest
        .rxSendForm(oidcTokenRequestBody)
        .onErrorResumeNext(
            err -> Single.error(INTERNAL_SERVER_ERROR.getCustomException(err.getMessage())))
        .map(
            res -> {
              if (res.statusCode() == 200) {
                JsonObject jsonBody = res.bodyAsJsonObject();
                if (StringUtils.isNotBlank(jsonBody.getString(OIDC_TOKENS_ID_TOKEN))) {
                  return IdpCredentials.builder()
                      .accessToken(jsonBody.getString(OIDC_TOKENS_ACCESS_TOKEN))
                      .refreshToken(jsonBody.getString(OIDC_REFRESH_TOKEN))
                      .idToken(jsonBody.getString(OIDC_TOKENS_ID_TOKEN))
                      .build();
                } else {
                  throw INVALID_IDP_CODE.getException();
                }
              } else if (res.statusCode() >= 400 && res.statusCode() < 500) {
                String error = res.bodyAsJsonObject().getString(ERROR);
                String errorDescription = res.bodyAsJsonObject().getString(ERROR_DESCRIPTION);
                throw PROVIDER_TOKENS_EXCHANGE_FAILED.getCustomException(error, errorDescription);
              } else {
                throw INTERNAL_SERVER_ERROR.getCustomException("Token exchange failed");
              }
            });
  }
}

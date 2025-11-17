package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.UNAUTHORIZED;
import static com.dreamsportslabs.guardian.utils.Utils.getAccessTokenFromAuthHeader;
import static com.dreamsportslabs.guardian.utils.Utils.getCurrentTimeInSeconds;

import com.dreamsportslabs.guardian.dao.CredentialDao;
import com.dreamsportslabs.guardian.dao.RefreshTokenDao;
import com.dreamsportslabs.guardian.dao.WebAuthnConfigDao;
import com.dreamsportslabs.guardian.dao.WebAuthnStateDao;
import com.dreamsportslabs.guardian.dao.model.CredentialModel;
import com.dreamsportslabs.guardian.dao.model.WebAuthnConfigModel;
import com.dreamsportslabs.guardian.dao.model.WebAuthnStateModel;
import com.dreamsportslabs.guardian.dto.request.v2.V2WebAuthnStartRequestDto;
import com.dreamsportslabs.guardian.dto.response.v2.V2WebAuthnStartResponseDto;
import com.dreamsportslabs.guardian.dto.response.v2.V2WebAuthnStartResponseDto.AssertBlock;
import com.dreamsportslabs.guardian.dto.response.v2.V2WebAuthnStartResponseDto.EnrollBlock;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import jakarta.ws.rs.core.HttpHeaders;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class WebAuthnService {
  private final RefreshTokenDao refreshTokenDao;
  private final CredentialDao credentialDao;
  private final WebAuthnConfigDao webauthnConfigDao;
  private final WebAuthnStateDao webauthnStateDao;

  // Constants
  private static final int CHALLENGE_BYTES = 32;
  private static final int STATE_TTL_SECONDS = 300; // 5 minutes
  private static final int WEBAUTHN_TIMEOUT_MS =
      60000; // 60 seconds (WebAuthn standard recommendation)
  private static final int STATE_RANDOM_LENGTH = 16;

  // State type constants
  private static final String STATE_TYPE_ASSERT = "assert";
  private static final String STATE_TYPE_ENROLL = "enroll";
  private static final String RECOMMENDED_MODE_ASSERT = "assert";
  private static final String RECOMMENDED_MODE_ENROLL = "enroll";

  // WebAuthn option keys
  private static final String KEY_CHALLENGE = "challenge";
  private static final String KEY_RP_ID = "rpId";
  private static final String KEY_USER_VERIFICATION = "userVerification";
  private static final String KEY_TIMEOUT = "timeout";
  private static final String KEY_ALLOW_CREDENTIALS = "allowCredentials";
  private static final String KEY_TYPE = "type";
  private static final String KEY_ID = "id";
  private static final String KEY_RP = "rp";
  private static final String KEY_USER = "user";
  private static final String KEY_PUB_KEY_CRED_PARAMS = "pubKeyCredParams";
  private static final String KEY_AUTHENTICATOR_SELECTION = "authenticatorSelection";
  private static final String KEY_ATTESTATION = "attestation";
  private static final String KEY_EXCLUDE_CREDENTIALS = "excludeCredentials";
  private static final String KEY_AUTHENTICATOR_ATTACHMENT = "authenticatorAttachment";
  private static final String KEY_RESIDENT_KEY = "residentKey";
  private static final String KEY_ALG = "alg";
  private static final String KEY_TRANSPORTS = "transports";

  // WebAuthn values
  private static final String VALUE_PUBLIC_KEY = "public-key";
  private static final String VALUE_REQUIRED = "required";
  private static final String VALUE_PREFERRED = "preferred";
  private static final String VALUE_PLATFORM = "platform";
  private static final String VALUE_ATTESTATION_NONE = "none";

  // Error messages
  private static final String ERROR_MISSING_AUTH_HEADER = "Missing Authorization header";
  private static final String ERROR_INVALID_REFRESH_TOKEN = "Invalid refresh token";
  private static final String ERROR_REFRESH_TOKEN_EXPIRED = "Refresh token expired";
  private static final String ERROR_WEBAUTHN_NOT_CONFIGURED =
      "WebAuthn not configured for this client";

  // COSE Algorithm IDs
  private static final int COSE_ALG_ES256 = -7;
  private static final int COSE_ALG_RS256 = -257;
  private static final int COSE_ALG_EDDSA = -8;

  /**
   * Start WebAuthn flow - returns assertion and/or enrollment options. rpId (Relying Party ID) is
   * the domain/identifier of the service that the user is authenticating with. It's used for origin
   * validation in WebAuthn to prevent phishing attacks.
   */
  public Single<V2WebAuthnStartResponseDto> start(
      V2WebAuthnStartRequestDto requestDto, HttpHeaders headers, String tenantId) {
    return validateAndGetRefreshToken(headers, tenantId, requestDto.getClientId())
        .flatMap(tokenModel -> getWebAuthnConfigAndCredentials(tenantId, tokenModel))
        .flatMap(context -> buildWebAuthnStartResponse(context, requestDto, tenantId));
  }

  private Single<RefreshTokenContext> validateAndGetRefreshToken(
      HttpHeaders headers, String tenantId, String clientId) {
    String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
    if (StringUtils.isBlank(authHeader)) {
      return Single.error(UNAUTHORIZED.getCustomException(ERROR_MISSING_AUTH_HEADER));
    }

    String refreshToken = getAccessTokenFromAuthHeader(authHeader);

    // If clientId is provided, use it; otherwise get from token
    if (StringUtils.isNotBlank(clientId)) {
      return refreshTokenDao
          .getRefreshToken(tenantId, clientId, refreshToken)
          .switchIfEmpty(Single.error(UNAUTHORIZED.getCustomException(ERROR_INVALID_REFRESH_TOKEN)))
          .filter(tokenModel -> tokenModel.getRefreshTokenExp() > getCurrentTimeInSeconds())
          .switchIfEmpty(Single.error(UNAUTHORIZED.getCustomException(ERROR_REFRESH_TOKEN_EXPIRED)))
          .map(
              tokenModel ->
                  RefreshTokenContext.builder()
                      .userId(tokenModel.getUserId())
                      .clientId(clientId)
                      .build());
    } else {
      // If clientId not provided, get from token (fallback)
      return refreshTokenDao
          .getRefreshToken(tenantId, refreshToken)
          .switchIfEmpty(Single.error(UNAUTHORIZED.getCustomException(ERROR_INVALID_REFRESH_TOKEN)))
          .filter(tokenModel -> tokenModel.getRefreshTokenExp() > getCurrentTimeInSeconds())
          .switchIfEmpty(Single.error(UNAUTHORIZED.getCustomException(ERROR_REFRESH_TOKEN_EXPIRED)))
          .map(
              tokenModel ->
                  RefreshTokenContext.builder()
                      .userId(tokenModel.getUserId())
                      .clientId(tokenModel.getClientId())
                      .build());
    }
  }

  private Single<WebAuthnContext> getWebAuthnConfigAndCredentials(
      String tenantId, RefreshTokenContext tokenContext) {
    return webauthnConfigDao
        .getWebAuthnConfig(tenantId, tokenContext.getClientId())
        .switchIfEmpty(
            Single.error(INVALID_REQUEST.getCustomException(ERROR_WEBAUTHN_NOT_CONFIGURED)))
        .flatMap(
            config ->
                credentialDao
                    .getActiveCredentialsByUserAndClient(
                        tenantId, tokenContext.getClientId(), tokenContext.getUserId())
                    .map(
                        credentials ->
                            WebAuthnContext.builder()
                                .config(config)
                                .credentials(credentials)
                                .userId(tokenContext.getUserId())
                                .clientId(tokenContext.getClientId())
                                .build()));
  }

  private Single<V2WebAuthnStartResponseDto> buildWebAuthnStartResponse(
      WebAuthnContext context, V2WebAuthnStartRequestDto requestDto, String tenantId) {
    String recommendedMode =
        context.getCredentials().isEmpty() ? RECOMMENDED_MODE_ENROLL : RECOMMENDED_MODE_ASSERT;

    // Generate assert block if credentials exist, otherwise always generate enroll block
    if (!context.getCredentials().isEmpty()) {
      return createAssertBlock(context, requestDto, tenantId)
          .flatMap(
              assertBlock ->
                  createEnrollBlock(context, requestDto, tenantId)
                      .map(
                          enrollBlock ->
                              V2WebAuthnStartResponseDto.builder()
                                  .recommendedMode(recommendedMode)
                                  .assertBlock(assertBlock)
                                  .enrollBlock(enrollBlock)
                                  .build()));
    } else {
      // Only enroll block needed
      return createEnrollBlock(context, requestDto, tenantId)
          .map(
              enrollBlock ->
                  V2WebAuthnStartResponseDto.builder()
                      .recommendedMode(recommendedMode)
                      .enrollBlock(enrollBlock)
                      .build());
    }
  }

  private Single<AssertBlock> createAssertBlock(
      WebAuthnContext context, V2WebAuthnStartRequestDto requestDto, String tenantId) {
    String assertState = generateState(STATE_TYPE_ASSERT);
    String assertChallenge = generateChallenge();

    WebAuthnStateModel assertStateModel =
        WebAuthnStateModel.builder()
            .state(assertState)
            .tenantId(tenantId)
            .clientId(context.getClientId())
            .userId(context.getUserId())
            .challenge(assertChallenge)
            .type(STATE_TYPE_ASSERT)
            .deviceMetadata(requestDto.getDeviceMetadata())
            .additionalInfo(new HashMap<>())
            .expiry(getCurrentTimeInSeconds() + STATE_TTL_SECONDS)
            .build();

    return webauthnStateDao
        .setWebAuthnState(assertStateModel, tenantId)
        .map(
            storedState -> {
              Map<String, Object> assertOptions =
                  buildAssertOptions(
                      context.getConfig(), context.getCredentials(), assertChallenge);
              return AssertBlock.builder().state(assertState).options(assertOptions).build();
            });
  }

  private Single<EnrollBlock> createEnrollBlock(
      WebAuthnContext context, V2WebAuthnStartRequestDto requestDto, String tenantId) {
    String enrollState = generateState(STATE_TYPE_ENROLL);
    String enrollChallenge = generateChallenge();

    WebAuthnStateModel enrollStateModel =
        WebAuthnStateModel.builder()
            .state(enrollState)
            .tenantId(tenantId)
            .clientId(context.getClientId())
            .userId(context.getUserId())
            .challenge(enrollChallenge)
            .type(STATE_TYPE_ENROLL)
            .deviceMetadata(requestDto.getDeviceMetadata())
            .additionalInfo(new HashMap<>())
            .expiry(getCurrentTimeInSeconds() + STATE_TTL_SECONDS)
            .build();

    return webauthnStateDao
        .setWebAuthnState(enrollStateModel, tenantId)
        .map(
            storedState -> {
              Map<String, Object> enrollOptions =
                  buildEnrollOptions(
                      context.getConfig(),
                      context.getUserId(),
                      enrollChallenge,
                      context.getCredentials());
              return EnrollBlock.builder().state(enrollState).options(enrollOptions).build();
            });
  }

  private String generateState(String prefix) {
    return prefix + "_" + RandomStringUtils.randomAlphanumeric(STATE_RANDOM_LENGTH);
  }

  private String generateChallenge() {
    SecureRandom random = new SecureRandom();
    byte[] challengeBytes = new byte[CHALLENGE_BYTES];
    random.nextBytes(challengeBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(challengeBytes);
  }

  private Map<String, Object> buildAssertOptions(
      WebAuthnConfigModel config, List<CredentialModel> credentials, String challenge) {
    Map<String, Object> options = new HashMap<>();
    options.put(KEY_CHALLENGE, challenge);
    options.put(KEY_RP_ID, config.getRpId());
    options.put(
        KEY_USER_VERIFICATION, config.getRequireUvAuth() ? VALUE_REQUIRED : VALUE_PREFERRED);
    options.put(KEY_TIMEOUT, WEBAUTHN_TIMEOUT_MS);

    List<Map<String, Object>> allowCredentials =
        credentials.stream()
            .map(
                cred -> {
                  Map<String, Object> credMap = new HashMap<>();
                  credMap.put(KEY_TYPE, VALUE_PUBLIC_KEY);
                  // Credential ID is base64url encoded string - client will decode to ArrayBuffer
                  credMap.put(KEY_ID, cred.getCredentialId());
                  // Add transports from config if available (hint for client)
                  if (config.getAllowedTransports() != null
                      && !config.getAllowedTransports().isEmpty()) {
                    credMap.put(KEY_TRANSPORTS, config.getAllowedTransports());
                  }
                  return credMap;
                })
            .toList();
    options.put(KEY_ALLOW_CREDENTIALS, allowCredentials);

    return options;
  }

  private Map<String, Object> buildEnrollOptions(
      WebAuthnConfigModel config,
      String userId,
      String challenge,
      List<CredentialModel> existingCredentials) {
    Map<String, Object> options = new HashMap<>();
    // Challenge is base64url encoded string - client will decode to ArrayBuffer
    options.put(KEY_CHALLENGE, challenge);
    options.put(KEY_RP, buildRpInfo(config));
    options.put(KEY_USER, buildUserInfo(userId));
    options.put(KEY_PUB_KEY_CRED_PARAMS, buildPubKeyCredParams(config));
    options.put(KEY_AUTHENTICATOR_SELECTION, buildAuthenticatorSelection(config));
    options.put(KEY_ATTESTATION, VALUE_ATTESTATION_NONE);
    // Exclude existing credentials to prevent duplicate enrollment
    options.put(KEY_EXCLUDE_CREDENTIALS, buildExcludeCredentials(existingCredentials, config));
    options.put(KEY_TIMEOUT, WEBAUTHN_TIMEOUT_MS);

    return options;
  }

  private Map<String, Object> buildRpInfo(WebAuthnConfigModel config) {
    Map<String, Object> rp = new HashMap<>();
    rp.put(KEY_ID, config.getRpId());
    // rpId is the Relying Party ID - the domain/identifier of the service
    // It's used for origin validation in WebAuthn to prevent phishing attacks
    return rp;
  }

  private Map<String, Object> buildUserInfo(String userId) {
    Map<String, Object> user = new HashMap<>();
    // User ID should be base64url encoded, max 64 bytes
    String userIdBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(userId.getBytes());
    user.put(KEY_ID, userIdBase64);
    return user;
  }

  private List<Map<String, Object>> buildPubKeyCredParams(WebAuthnConfigModel config) {
    return config.getAllowedAlgorithms().stream()
        .map(
            alg -> {
              Map<String, Object> param = new HashMap<>();
              param.put(KEY_TYPE, VALUE_PUBLIC_KEY);
              param.put(KEY_ALG, getCoseAlgorithmId(alg));
              return param;
            })
        .toList();
  }

  private Map<String, Object> buildAuthenticatorSelection(WebAuthnConfigModel config) {
    Map<String, Object> authenticatorSelection = new HashMap<>();
    if (config.getRequireDeviceBound()) {
      authenticatorSelection.put(KEY_AUTHENTICATOR_ATTACHMENT, VALUE_PLATFORM);
    }
    authenticatorSelection.put(
        KEY_USER_VERIFICATION, config.getRequireUvEnrollment() ? VALUE_REQUIRED : VALUE_PREFERRED);
    authenticatorSelection.put(KEY_RESIDENT_KEY, VALUE_PREFERRED);
    return authenticatorSelection;
  }

  private List<Map<String, Object>> buildExcludeCredentials(
      List<CredentialModel> existingCredentials, WebAuthnConfigModel config) {
    return existingCredentials.stream()
        .map(
            cred -> {
              Map<String, Object> credMap = new HashMap<>();
              credMap.put(KEY_TYPE, VALUE_PUBLIC_KEY);
              // Credential ID is base64url encoded string - client will decode to ArrayBuffer
              credMap.put(KEY_ID, cred.getCredentialId());
              // Add transports from config if available (hint for client)
              if (config.getAllowedTransports() != null
                  && !config.getAllowedTransports().isEmpty()) {
                credMap.put(KEY_TRANSPORTS, config.getAllowedTransports());
              }
              return credMap;
            })
        .toList();
  }

  private int getCoseAlgorithmId(String algorithmName) {
    return switch (algorithmName.toUpperCase()) {
      case "ES256" -> COSE_ALG_ES256;
      case "RS256" -> COSE_ALG_RS256;
      case "EDDSA" -> COSE_ALG_EDDSA;
      default -> COSE_ALG_ES256; // Default to ES256
    };
  }

  // Helper classes for context passing
  @Builder
  @Getter
  private static class RefreshTokenContext {
    private String userId;
    private String clientId;
  }

  @Builder
  @Getter
  private static class WebAuthnContext {
    private WebAuthnConfigModel config;
    private List<CredentialModel> credentials;
    private String userId;
    private String clientId;
  }
}

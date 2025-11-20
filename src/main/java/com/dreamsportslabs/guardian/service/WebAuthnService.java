package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.TOKEN_TYPE;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_AAGUID_POLICY_MODE_ALLOWLIST;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_AAGUID_POLICY_MODE_ANY;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_AAGUID_POLICY_MODE_MDS_ENFORCED;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_BINDING_TYPE_WEBAUTHN;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_CLIENT_DATA_TYPE_CREATE;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_ERROR_CLIENT_NOT_FOUND;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_ERROR_CREDENTIAL_NOT_FOUND;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_ERROR_DUPLICATE_CREDENTIAL;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_ERROR_INVALID_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_ERROR_INVALID_STATE;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_ERROR_INVALID_STATE_TYPE;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_ERROR_INVALID_TRANSPORT;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_ERROR_MFA_REQUIRED;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_ERROR_MISSING_AUTH_HEADER;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_ERROR_NOT_CONFIGURED;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_ERROR_REFRESH_TOKEN_EXPIRED;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_ERROR_SIGN_COUNT_REPLAY;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_ERROR_STATE_CLIENT_MISMATCH;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_ERROR_STATE_EXPIRED;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_ERROR_STATE_USER_MISMATCH;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_ERROR_USER_VERIFICATION_REQUIRED;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_ERROR_VERIFICATION_FAILED;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_JSON_KEY_AAGUID;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_JSON_KEY_ACCESS_TOKEN;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_JSON_KEY_ATTESTATION_OBJECT;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_JSON_KEY_AUTHENTICATOR_DATA;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_JSON_KEY_CHALLENGE;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_JSON_KEY_CLIENT_DATA_JSON;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_JSON_KEY_DOMAIN;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_JSON_KEY_EXPIRES_IN;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_JSON_KEY_ID;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_JSON_KEY_ID_TOKEN;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_JSON_KEY_ORIGIN;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_JSON_KEY_RAW_ID;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_JSON_KEY_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_JSON_KEY_RESPONSE;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_JSON_KEY_SIGNATURE;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_JSON_KEY_TOKEN_TYPE;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_JSON_KEY_TRANSPORT;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_JSON_KEY_TYPE;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_JSON_KEY_USERNAME;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_JSON_KEY_USER_HANDLE;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_JSON_KEY_UV;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_JSON_KEY_WEBAUTHN;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_KEY_ALG;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_KEY_ALLOW_CREDENTIALS;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_KEY_ATTESTATION;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_KEY_AUTHENTICATOR_ATTACHMENT;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_KEY_AUTHENTICATOR_SELECTION;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_KEY_CHALLENGE;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_KEY_DISPLAY_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_KEY_EXCLUDE_CREDENTIALS;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_KEY_ID;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_KEY_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_KEY_PUB_KEY_CRED_PARAMS;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_KEY_RESIDENT_KEY;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_KEY_RP;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_KEY_RP_ID;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_KEY_TIMEOUT;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_KEY_TRANSPORTS;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_KEY_TYPE;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_KEY_USER;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_KEY_USER_VERIFICATION;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_MFA_POLICY_MANDATORY;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_RECOMMENDED_MODE_ASSERT;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_RECOMMENDED_MODE_ENROLL;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_STATE_TYPE_ASSERT;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_STATE_TYPE_ENROLL;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_VALUE_ATTESTATION_DIRECT;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_VALUE_PLATFORM;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_VALUE_PREFERRED;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_VALUE_PUBLIC_KEY;
import static com.dreamsportslabs.guardian.constant.Constants.WEBAUTHN_VALUE_REQUIRED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.UNAUTHORIZED;
import static com.dreamsportslabs.guardian.utils.Utils.getAccessTokenFromAuthHeader;
import static com.dreamsportslabs.guardian.utils.Utils.getCurrentTimeInSeconds;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.UnicodeString;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.constant.AuthMethod;
import com.dreamsportslabs.guardian.dao.ClientDao;
import com.dreamsportslabs.guardian.dao.CredentialDao;
import com.dreamsportslabs.guardian.dao.RefreshTokenDao;
import com.dreamsportslabs.guardian.dao.WebAuthnStateDao;
import com.dreamsportslabs.guardian.dao.model.ClientModel;
import com.dreamsportslabs.guardian.dao.model.CredentialModel;
import com.dreamsportslabs.guardian.dao.model.RefreshTokenModel;
import com.dreamsportslabs.guardian.dao.model.WebAuthnConfigModel;
import com.dreamsportslabs.guardian.dao.model.WebAuthnStateModel;
import com.dreamsportslabs.guardian.dto.request.v2.V2WebAuthnFinishRequestDto;
import com.dreamsportslabs.guardian.dto.request.v2.V2WebAuthnStartRequestDto;
import com.dreamsportslabs.guardian.dto.response.v2.V2WebAuthnStartResponseDto;
import com.dreamsportslabs.guardian.dto.response.v2.V2WebAuthnStartResponseDto.AssertBlock;
import com.dreamsportslabs.guardian.dto.response.v2.V2WebAuthnStartResponseDto.EnrollBlock;
import com.dreamsportslabs.guardian.registry.Registry;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.webauthn.Authenticator;
import io.vertx.ext.auth.webauthn.RelyingParty;
import io.vertx.ext.auth.webauthn.WebAuthn;
import io.vertx.ext.auth.webauthn.WebAuthnOptions;
import io.vertx.rxjava3.core.Vertx;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
  private final WebAuthnStateDao webauthnStateDao;
  private final ClientDao clientDao;
  private final TokenIssuer tokenIssuer;
  private final UserService userService;
  private final Registry registry;
  private final Vertx vertx;

  private static final int CHALLENGE_BYTES = 32;
  private static final int STATE_TTL_SECONDS = 300;
  private static final int WEBAUTHN_TIMEOUT_MS = 60000;
  private static final int STATE_RANDOM_LENGTH = 16;

  private static final int COSE_ALG_ES256 = -7;
  private static final int COSE_ALG_RS256 = -257;
  private static final int COSE_ALG_EDDSA = -8;

  /**
   * Starts the WebAuthn flow by returning assertion and/or enrollment options.
   *
   * @param requestDto the start request containing client ID
   * @param headers HTTP headers containing the refresh token
   * @param tenantId the tenant identifier
   * @return Single containing the WebAuthn start response with assertion and/or enrollment options
   */
  public Single<V2WebAuthnStartResponseDto> start(
      V2WebAuthnStartRequestDto requestDto, HttpHeaders headers, String tenantId) {
    return validateAndGetRefreshToken(headers, tenantId, requestDto.getClientId())
        .flatMap(tokenModel -> getWebAuthnConfigAndCredentials(tenantId, tokenModel))
        .flatMap(context -> buildWebAuthnStartResponse(context, requestDto, tenantId));
  }

  /**
   * Validates and retrieves the refresh token from the Authorization header.
   *
   * @param headers HTTP headers containing the Authorization header
   * @param tenantId the tenant identifier
   * @param clientId the client identifier
   * @return Single containing the refresh token context
   */
  private Single<RefreshTokenContext> validateAndGetRefreshToken(
      HttpHeaders headers, String tenantId, String clientId) {
    String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
    if (StringUtils.isBlank(authHeader)) {
      return Single.error(UNAUTHORIZED.getCustomException(WEBAUTHN_ERROR_MISSING_AUTH_HEADER));
    }

    String refreshToken = getAccessTokenFromAuthHeader(authHeader);
    return refreshTokenDao
        .getRefreshToken(tenantId, clientId, refreshToken)
        .switchIfEmpty(
            Single.error(UNAUTHORIZED.getCustomException(WEBAUTHN_ERROR_INVALID_REFRESH_TOKEN)))
        .filter(tokenModel -> tokenModel.getRefreshTokenExp() > getCurrentTimeInSeconds())
        .switchIfEmpty(
            Single.error(UNAUTHORIZED.getCustomException(WEBAUTHN_ERROR_REFRESH_TOKEN_EXPIRED)))
        .map(
            tokenModel ->
                RefreshTokenContext.builder()
                    .userId(tokenModel.getUserId())
                    .clientId(clientId)
                    .refreshToken(tokenModel)
                    .build());
  }

  /**
   * Retrieves WebAuthn configuration and active credentials for the user.
   *
   * @param tenantId the tenant identifier
   * @param tokenContext the refresh token context containing user and client information
   * @return Single containing the WebAuthn context with config and credentials
   */
  private Single<WebAuthnContext> getWebAuthnConfigAndCredentials(
      String tenantId, RefreshTokenContext tokenContext) {
    TenantConfig tenantConfig = registry.get(tenantId, TenantConfig.class);
    if (tenantConfig == null) {
      return Single.error(INVALID_REQUEST.getCustomException(WEBAUTHN_ERROR_NOT_CONFIGURED));
    }

    Map<String, WebAuthnConfigModel> webauthnConfigs = tenantConfig.getWebauthnConfig();
    if (webauthnConfigs == null || webauthnConfigs.isEmpty()) {
      return Single.error(INVALID_REQUEST.getCustomException(WEBAUTHN_ERROR_NOT_CONFIGURED));
    }

    WebAuthnConfigModel config = webauthnConfigs.get(tokenContext.getClientId());
    if (config == null) {
      return Single.error(INVALID_REQUEST.getCustomException(WEBAUTHN_ERROR_NOT_CONFIGURED));
    }

    return credentialDao
        .getActiveCredentialsByUserAndClient(
            tenantId, tokenContext.getClientId(), tokenContext.getUserId())
        .map(
            credentials ->
                WebAuthnContext.builder()
                    .config(config)
                    .credentials(credentials)
                    .userId(tokenContext.getUserId())
                    .clientId(tokenContext.getClientId())
                    .build());
  }

  /**
   * Builds the WebAuthn start response with assertion and/or enrollment blocks.
   *
   * @param context the WebAuthn context containing config and credentials
   * @param requestDto the start request DTO
   * @param tenantId the tenant identifier
   * @return Single containing the WebAuthn start response
   */
  private Single<V2WebAuthnStartResponseDto> buildWebAuthnStartResponse(
      WebAuthnContext context, V2WebAuthnStartRequestDto requestDto, String tenantId) {
    String recommendedMode =
        context.getCredentials().isEmpty()
            ? WEBAUTHN_RECOMMENDED_MODE_ENROLL
            : WEBAUTHN_RECOMMENDED_MODE_ASSERT;
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
      return createEnrollBlock(context, requestDto, tenantId)
          .map(
              enrollBlock ->
                  V2WebAuthnStartResponseDto.builder()
                      .recommendedMode(recommendedMode)
                      .enrollBlock(enrollBlock)
                      .build());
    }
  }

  /**
   * Creates an assertion block for WebAuthn authentication.
   *
   * @param context the WebAuthn context
   * @param requestDto the start request DTO
   * @param tenantId the tenant identifier
   * @return Single containing the assertion block with state and options
   */
  private Single<AssertBlock> createAssertBlock(
      WebAuthnContext context, V2WebAuthnStartRequestDto requestDto, String tenantId) {
    String assertState = generateState(WEBAUTHN_STATE_TYPE_ASSERT);
    String assertChallenge = generateChallenge();

    WebAuthnStateModel assertStateModel =
        createWebAuthnStateModel(
            assertState,
            tenantId,
            context.getClientId(),
            context.getUserId(),
            assertChallenge,
            WEBAUTHN_STATE_TYPE_ASSERT,
            requestDto.getDeviceMetadata());

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

  /**
   * Creates an enrollment block for WebAuthn credential registration.
   *
   * @param context the WebAuthn context
   * @param requestDto the start request DTO
   * @param tenantId the tenant identifier
   * @return Single containing the enrollment block with state and options
   */
  private Single<EnrollBlock> createEnrollBlock(
      WebAuthnContext context, V2WebAuthnStartRequestDto requestDto, String tenantId) {
    String enrollState = generateState(WEBAUTHN_STATE_TYPE_ENROLL);
    String enrollChallenge = generateChallenge();

    WebAuthnStateModel enrollStateModel =
        createWebAuthnStateModel(
            enrollState,
            tenantId,
            context.getClientId(),
            context.getUserId(),
            enrollChallenge,
            WEBAUTHN_STATE_TYPE_ENROLL,
            requestDto.getDeviceMetadata());

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

  /**
   * Creates a WebAuthn state model with the provided parameters.
   *
   * @param state the state string
   * @param tenantId the tenant identifier
   * @param clientId the client identifier
   * @param userId the user identifier
   * @param challenge the challenge string
   * @param type the state type (enrollment or assertion)
   * @param deviceMetadata the device metadata
   * @return the WebAuthn state model
   */
  private WebAuthnStateModel createWebAuthnStateModel(
      String state,
      String tenantId,
      String clientId,
      String userId,
      String challenge,
      String type,
      com.dreamsportslabs.guardian.dto.request.DeviceMetadata deviceMetadata) {
    return WebAuthnStateModel.builder()
        .state(state)
        .tenantId(tenantId)
        .clientId(clientId)
        .userId(userId)
        .challenge(challenge)
        .type(type)
        .deviceMetadata(deviceMetadata)
        .additionalInfo(new HashMap<>())
        .expiry(getCurrentTimeInSeconds() + STATE_TTL_SECONDS)
        .build();
  }

  /**
   * Generates a unique state string for WebAuthn flow.
   *
   * @param prefix the prefix for the state (e.g., "assert" or "enroll")
   * @return a unique state string
   */
  private String generateState(String prefix) {
    return prefix + "_" + RandomStringUtils.randomAlphanumeric(STATE_RANDOM_LENGTH);
  }

  /**
   * Generates a cryptographically secure random challenge for WebAuthn.
   *
   * @return base64url-encoded challenge string
   */
  private String generateChallenge() {
    SecureRandom random = new SecureRandom();
    byte[] challengeBytes = new byte[CHALLENGE_BYTES];
    random.nextBytes(challengeBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(challengeBytes);
  }

  /**
   * Builds assertion options for WebAuthn authentication.
   *
   * @param config the WebAuthn configuration
   * @param credentials the list of existing credentials for the user
   * @param challenge the challenge string
   * @return map containing assertion options
   */
  private Map<String, Object> buildAssertOptions(
      WebAuthnConfigModel config, List<CredentialModel> credentials, String challenge) {
    Map<String, Object> options = new HashMap<>();
    options.put(WEBAUTHN_KEY_CHALLENGE, challenge);
    options.put(WEBAUTHN_KEY_RP_ID, config.getRpId());
    options.put(
        WEBAUTHN_KEY_USER_VERIFICATION,
        config.getRequireUvAuth() ? WEBAUTHN_VALUE_REQUIRED : WEBAUTHN_VALUE_PREFERRED);
    options.put(WEBAUTHN_KEY_TIMEOUT, WEBAUTHN_TIMEOUT_MS);

    List<Map<String, Object>> allowCredentials =
        credentials.stream().map(cred -> buildCredentialMap(cred, config)).toList();
    options.put(WEBAUTHN_KEY_ALLOW_CREDENTIALS, allowCredentials);

    return options;
  }

  /**
   * Builds enrollment options for WebAuthn credential registration.
   *
   * @param config the WebAuthn configuration
   * @param userId the user identifier
   * @param challenge the challenge string
   * @param existingCredentials the list of existing credentials to exclude
   * @return map containing enrollment options
   */
  private Map<String, Object> buildEnrollOptions(
      WebAuthnConfigModel config,
      String userId,
      String challenge,
      List<CredentialModel> existingCredentials) {
    Map<String, Object> options = new HashMap<>();
    options.put(WEBAUTHN_KEY_CHALLENGE, challenge);
    options.put(WEBAUTHN_KEY_RP, buildRpInfo(config));
    options.put(WEBAUTHN_KEY_USER, buildUserInfo(userId));
    options.put(WEBAUTHN_KEY_PUB_KEY_CRED_PARAMS, buildPubKeyCredParams(config));
    options.put(WEBAUTHN_KEY_AUTHENTICATOR_SELECTION, buildAuthenticatorSelection(config));
    options.put(WEBAUTHN_KEY_ATTESTATION, determineAttestationPreference(config));
    options.put(
        WEBAUTHN_KEY_EXCLUDE_CREDENTIALS, buildExcludeCredentials(existingCredentials, config));
    options.put(WEBAUTHN_KEY_TIMEOUT, WEBAUTHN_TIMEOUT_MS);

    return options;
  }

  /**
   * Builds relying party information for WebAuthn options.
   *
   * @param config the WebAuthn configuration
   * @return map containing relying party information
   */
  private Map<String, Object> buildRpInfo(WebAuthnConfigModel config) {
    Map<String, Object> rp = new HashMap<>();
    rp.put(WEBAUTHN_KEY_ID, config.getRpId());
    rp.put(WEBAUTHN_KEY_NAME, config.getRpId());
    return rp;
  }

  /**
   * Builds user information for WebAuthn enrollment options.
   *
   * @param userId the user identifier
   * @return map containing user information with base64url-encoded ID
   */
  private Map<String, Object> buildUserInfo(String userId) {
    Map<String, Object> user = new HashMap<>();
    String userIdBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(userId.getBytes());
    user.put(WEBAUTHN_KEY_ID, userIdBase64);
    user.put(WEBAUTHN_KEY_NAME, userId);
    user.put(WEBAUTHN_KEY_DISPLAY_NAME, userId);
    return user;
  }

  /**
   * Builds public key credential parameters from allowed algorithms.
   *
   * @param config the WebAuthn configuration
   * @return list of public key credential parameter maps
   */
  private List<Map<String, Object>> buildPubKeyCredParams(WebAuthnConfigModel config) {
    return config.getAllowedAlgorithms().stream()
        .map(
            alg -> {
              Map<String, Object> param = new HashMap<>();
              param.put(WEBAUTHN_KEY_TYPE, WEBAUTHN_VALUE_PUBLIC_KEY);
              param.put(WEBAUTHN_KEY_ALG, getCoseAlgorithmId(alg));
              return param;
            })
        .toList();
  }

  /**
   * Builds authenticator selection criteria for WebAuthn enrollment.
   *
   * @param config the WebAuthn configuration
   * @return map containing authenticator selection criteria
   */
  private Map<String, Object> buildAuthenticatorSelection(WebAuthnConfigModel config) {
    Map<String, Object> authenticatorSelection = new HashMap<>();
    if (config.getRequireDeviceBound()) {
      authenticatorSelection.put(WEBAUTHN_KEY_AUTHENTICATOR_ATTACHMENT, WEBAUTHN_VALUE_PLATFORM);
    }
    authenticatorSelection.put(
        WEBAUTHN_KEY_USER_VERIFICATION,
        config.getRequireUvEnrollment() ? WEBAUTHN_VALUE_REQUIRED : WEBAUTHN_VALUE_PREFERRED);
    authenticatorSelection.put(WEBAUTHN_KEY_RESIDENT_KEY, WEBAUTHN_VALUE_PREFERRED);
    return authenticatorSelection;
  }

  /**
   * Builds exclude credentials list to prevent duplicate enrollment.
   *
   * @param existingCredentials the list of existing credentials
   * @param config the WebAuthn configuration
   * @return list of credential maps to exclude
   */
  private List<Map<String, Object>> buildExcludeCredentials(
      List<CredentialModel> existingCredentials, WebAuthnConfigModel config) {
    return existingCredentials.stream().map(cred -> buildCredentialMap(cred, config)).toList();
  }

  /**
   * Builds a credential map for WebAuthn options (used in allowCredentials and excludeCredentials).
   *
   * @param credential the credential model
   * @param config the WebAuthn configuration
   * @return the credential map
   */
  private Map<String, Object> buildCredentialMap(
      CredentialModel credential, WebAuthnConfigModel config) {
    Map<String, Object> credMap = new HashMap<>();
    credMap.put(WEBAUTHN_KEY_TYPE, WEBAUTHN_VALUE_PUBLIC_KEY);
    credMap.put(WEBAUTHN_KEY_ID, credential.getCredentialId());
    if (config.getAllowedTransports() != null && !config.getAllowedTransports().isEmpty()) {
      credMap.put(WEBAUTHN_KEY_TRANSPORTS, config.getAllowedTransports());
    }
    return credMap;
  }

  /**
   * Determines attestation preference based on AAGUID policy configuration.
   *
   * <p>If AAGUID validation is required, "direct" attestation is used to receive the AAGUID from
   * the authenticator. Platform authenticators may return fmt="none" with non-zero AAGUID, which
   * violates the WebAuthn spec.
   *
   * <p>Currently always returns "direct" to ensure AAGUID is available for validation.
   *
   * @param config WebAuthn configuration
   * @return attestation preference string ("direct")
   */
  private String determineAttestationPreference(WebAuthnConfigModel config) {
    return WEBAUTHN_VALUE_ATTESTATION_DIRECT;
  }

  /**
   * Converts algorithm name to COSE algorithm ID.
   *
   * @param algorithmName the algorithm name (e.g., "ES256", "RS256", "EDDSA")
   * @return the COSE algorithm ID, defaults to ES256 if unknown
   */
  private int getCoseAlgorithmId(String algorithmName) {
    return switch (algorithmName.toUpperCase()) {
      case "ES256" -> COSE_ALG_ES256;
      case "RS256" -> COSE_ALG_RS256;
      case "EDDSA" -> COSE_ALG_EDDSA;
      default -> COSE_ALG_ES256;
    };
  }

  /**
   * Finishes the WebAuthn flow by verifying assertion or attestation.
   *
   * <p>Includes MFA policy check: if client MFA is mandatory and refresh token has only one AMR,
   * the request will be rejected.
   *
   * @param requestDto the finish request containing credential and state
   * @param headers HTTP headers containing the refresh token
   * @param tenantId the tenant identifier
   * @return Single containing the response with access token, refresh token, and optionally ID
   *     token
   */
  public Single<Map<String, Object>> finish(
      V2WebAuthnFinishRequestDto requestDto, HttpHeaders headers, String tenantId) {
    MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
    return validateAndGetRefreshToken(headers, tenantId, requestDto.getClientId())
        .flatMap(
            tokenContext ->
                Single.zip(
                    Single.just(tokenContext),
                    webauthnStateDao
                        .getWebAuthnState(requestDto.getState(), tenantId)
                        .switchIfEmpty(
                            Single.error(
                                INVALID_REQUEST.getCustomException(WEBAUTHN_ERROR_INVALID_STATE))),
                    clientDao
                        .getClient(requestDto.getClientId(), tenantId)
                        .switchIfEmpty(
                            Single.error(
                                INVALID_REQUEST.getCustomException(
                                    WEBAUTHN_ERROR_CLIENT_NOT_FOUND))),
                    FinishContext::new))
        .flatMap(this::validateState)
        .flatMap(this::checkMfaPolicy)
        .flatMap(
            context ->
                getWebAuthnConfigAndCredentials(tenantId, context.getTokenContext())
                    .map(
                        webAuthnContext ->
                            FinishContextWithConfig.builder()
                                .tokenContext(context.getTokenContext())
                                .state(context.getState())
                                .client(context.getClient())
                                .webAuthnContext(webAuthnContext)
                                .build()))
        .flatMap(
            context ->
                verifyWebAuthnCredential(context, requestDto, tenantId, headers)
                    .flatMap(
                        verifiedContext ->
                            processWebAuthnResult(
                                verifiedContext, requestDto, tenantId, requestHeaders)));
  }

  /**
   * Validates WebAuthn state: expiry, user ID, client ID, and type.
   *
   * @param context the finish context containing state and token context
   * @return Single containing the validated context
   */
  private Single<FinishContext> validateState(FinishContext context) {
    WebAuthnStateModel state = context.getState();
    RefreshTokenContext tokenContext = context.getTokenContext();

    if (state.getExpiry() <= getCurrentTimeInSeconds()) {
      webauthnStateDao.deleteWebAuthnState(state.getState(), state.getTenantId());
      return Single.error(INVALID_REQUEST.getCustomException(WEBAUTHN_ERROR_STATE_EXPIRED));
    }

    if (!state.getUserId().equals(tokenContext.getUserId())) {
      return Single.error(INVALID_REQUEST.getCustomException(WEBAUTHN_ERROR_STATE_USER_MISMATCH));
    }

    if (!state.getClientId().equals(context.getTokenContext().getClientId())) {
      return Single.error(INVALID_REQUEST.getCustomException(WEBAUTHN_ERROR_STATE_CLIENT_MISMATCH));
    }

    return Single.just(context);
  }

  /**
   * Checks MFA policy: rejects if MFA is mandatory and refresh token has only one AMR.
   *
   * @param context the finish context
   * @return Single containing the context if MFA policy is satisfied
   */
  private Single<FinishContext> checkMfaPolicy(FinishContext context) {
    ClientModel client = context.getClient();
    RefreshTokenModel refreshToken = context.getTokenContext().getRefreshToken();

    if (WEBAUTHN_MFA_POLICY_MANDATORY.equals(client.getMfaPolicy())) {
      List<AuthMethod> authMethods = refreshToken.getAuthMethod();
      if (authMethods == null || authMethods.size() <= 1) {
        return Single.error(INVALID_REQUEST.getCustomException(WEBAUTHN_ERROR_MFA_REQUIRED));
      }
    }

    return Single.just(context);
  }

  /**
   * Routes to appropriate verification method based on state type (enrollment or assertion).
   *
   * @param context the finish context with config
   * @param requestDto the finish request DTO
   * @param tenantId the tenant identifier
   * @param headers HTTP headers
   * @return Single containing the verified context
   */
  private Single<FinishContextWithConfig> verifyWebAuthnCredential(
      FinishContextWithConfig context,
      V2WebAuthnFinishRequestDto requestDto,
      String tenantId,
      HttpHeaders headers) {
    WebAuthnStateModel state = context.getState();

    if (WEBAUTHN_STATE_TYPE_ENROLL.equals(state.getType())) {
      return verifyEnrollment(context, requestDto, tenantId, headers);
    } else if (WEBAUTHN_STATE_TYPE_ASSERT.equals(state.getType())) {
      return verifyAssertion(context, requestDto, tenantId);
    } else {
      return Single.error(
          INVALID_REQUEST.getCustomException(
              String.format(WEBAUTHN_ERROR_INVALID_STATE_TYPE, state.getType())));
    }
  }

  /**
   * Handles enrollment verification errors, including AAGUID validation workarounds.
   *
   * @param err the error that occurred during verification
   * @param context the finish context with config
   * @param requestDto the finish request DTO
   * @param config the WebAuthn configuration
   * @param tenantId the tenant identifier
   * @param state the WebAuthn state
   * @param headers HTTP headers
   * @param credentialId the credential ID
   * @return Single containing the verified context if error can be handled, or error otherwise
   */
  private Single<FinishContextWithConfig> handleEnrollmentError(
      Throwable err,
      FinishContextWithConfig context,
      V2WebAuthnFinishRequestDto requestDto,
      WebAuthnConfigModel config,
      String tenantId,
      WebAuthnStateModel state,
      HttpHeaders headers,
      String credentialId) {
    log.error("WebAuthn enrollment verification failed", err);

    String errorMessage = err.getMessage();
    String errorClass = err.getClass().getName();

    if (isAaguidError(errorClass, errorMessage)) {
      return handleAaguidError(
          errorClass, errorMessage, context, requestDto, config, tenantId, state, headers);
    }

    if (isDuplicateCredentialError(errorMessage)) {
      log.warn(
          "Duplicate credential detected by WebAuthn library. Credential ID: {}", credentialId);
      return Single.error(
          INVALID_REQUEST.getCustomException(
              WEBAUTHN_ERROR_DUPLICATE_CREDENTIAL
                  + ". This credential is already registered. "
                  + "Credential ID: "
                  + credentialId));
    }

    return Single.error(
        INVALID_REQUEST.getCustomException(
            WEBAUTHN_ERROR_VERIFICATION_FAILED + ": " + errorMessage));
  }

  /**
   * Checks if an error is an AAGUID-related error.
   *
   * @param errorClass the error class name
   * @param errorMessage the error message
   * @return true if the error is AAGUID-related
   */
  private boolean isAaguidError(String errorClass, String errorMessage) {
    if (errorClass != null && errorClass.contains("AttestationException")) {
      log.debug("Detected AttestationException by class name: {}", errorClass);
      return true;
    }
    if (errorMessage != null
        && (errorMessage.contains("AAGUID is not 00000000-0000-0000-0000-000000000000")
            || errorMessage.contains("AAGUID")
            || errorMessage.contains("00000000-0000-0000-0000-000000000000"))) {
      log.debug("Detected AAGUID error by message: {}", errorMessage);
      return true;
    }
    return false;
  }

  /**
   * Checks if an error is a duplicate credential error.
   *
   * @param errorMessage the error message
   * @return true if the error is duplicate credential-related
   */
  private boolean isDuplicateCredentialError(String errorMessage) {
    return errorMessage != null
        && (errorMessage.contains("already registered")
            || errorMessage.contains("duplicate")
            || errorMessage.contains("excludeCredentials"));
  }

  /**
   * Handles AAGUID validation errors, potentially bypassing validation for platform authenticators.
   *
   * @param errorClass the error class name
   * @param errorMessage the error message
   * @param context the finish context with config
   * @param requestDto the finish request DTO
   * @param config the WebAuthn configuration
   * @param tenantId the tenant identifier
   * @param state the WebAuthn state
   * @param headers HTTP headers
   * @return Single containing the verified context if bypass is allowed, or error otherwise
   */
  private Single<FinishContextWithConfig> handleAaguidError(
      String errorClass,
      String errorMessage,
      FinishContextWithConfig context,
      V2WebAuthnFinishRequestDto requestDto,
      WebAuthnConfigModel config,
      String tenantId,
      WebAuthnStateModel state,
      HttpHeaders headers) {
    log.warn(
        "Platform authenticator returned fmt='none' with non-zero AAGUID. "
            + "This violates WebAuthn spec but is common with Touch ID/Face ID. "
            + "Error class: {}, Error message: {}, "
            + "AAGUID policy mode: {}, Allowed AAGUIDs: {}, Blocked AAGUIDs: {}",
        errorClass,
        errorMessage,
        config.getAaguidPolicyMode(),
        config.getAllowedAaguids(),
        config.getBlockedAaguids());

    String policyMode = config.getAaguidPolicyMode();
    boolean hasRestrictions = hasAaguidRestrictions(config, policyMode);

    log.info(
        "AAGUID error detected. Policy mode: {}, Has restrictions: {}, "
            + "Policy mode equals ANY: {}",
        policyMode,
        hasRestrictions,
        WEBAUTHN_AAGUID_POLICY_MODE_ANY.equals(policyMode));

    if (WEBAUTHN_AAGUID_POLICY_MODE_ANY.equals(policyMode) && !hasRestrictions) {
      log.info(
          "AAGUID check disabled (policy: any). Bypassing library validation "
              + "and performing minimal custom validation for platform authenticator.");
      return bypassAaguidValidationAndEnroll(context, requestDto, config, tenantId, state, headers)
          .flatMap(Single::just);
    } else {
      return Single.error(
          INVALID_REQUEST.getCustomException(
              "Attestation validation failed: Platform authenticator returned "
                  + "fmt='none' with non-zero AAGUID. AAGUID validation is required by your "
                  + "configuration (policy: "
                  + policyMode
                  + ", hasRestrictions: "
                  + hasRestrictions
                  + "). "
                  + "Please try a different authenticator or adjust your AAGUID policy."));
    }
  }

  /**
   * Checks if AAGUID restrictions are configured.
   *
   * @param config the WebAuthn configuration
   * @param policyMode the AAGUID policy mode
   * @return true if restrictions are configured
   */
  private boolean hasAaguidRestrictions(WebAuthnConfigModel config, String policyMode) {
    return (config.getBlockedAaguids() != null && !config.getBlockedAaguids().isEmpty())
        || (config.getAllowedAaguids() != null && !config.getAllowedAaguids().isEmpty())
        || WEBAUTHN_AAGUID_POLICY_MODE_ALLOWLIST.equals(policyMode)
        || WEBAUTHN_AAGUID_POLICY_MODE_MDS_ENFORCED.equals(policyMode);
  }

  /**
   * Verifies enrollment (attestation) by verifying challenge is bound by public key, then saves to
   * DB.
   *
   * @param context the finish context with config
   * @param requestDto the finish request DTO
   * @param tenantId the tenant identifier
   * @param headers HTTP headers
   * @return Single containing the verified context
   */
  private Single<FinishContextWithConfig> verifyEnrollment(
      FinishContextWithConfig context,
      V2WebAuthnFinishRequestDto requestDto,
      String tenantId,
      HttpHeaders headers) {
    WebAuthnConfigModel config = context.getWebAuthnContext().getConfig();
    WebAuthnStateModel state = context.getState();

    String credentialId = requestDto.getCredential().getId();
    return credentialDao
        .getCredentialById(
            tenantId, requestDto.getClientId(), context.getTokenContext().getUserId(), credentialId)
        .flatMap(
            existingCredential ->
                Maybe.<FinishContextWithConfig>error(
                    INVALID_REQUEST.getCustomException(
                        WEBAUTHN_ERROR_DUPLICATE_CREDENTIAL
                            + ". Credential ID: "
                            + credentialId
                            + " already exists for this user.")))
        .switchIfEmpty(Maybe.just(context))
        .toSingle()
        .flatMap(
            (FinishContextWithConfig ctx) -> {
              WebAuthn webAuthn = createWebAuthnInstance(config);

              return buildAuthRequest(ctx, state, requestDto, config)
                  .flatMap(
                      authRequest -> {
                        webAuthn.authenticatorFetcher(
                            query -> Future.succeededFuture(new ArrayList<>()));

                        final Authenticator[] storedAuthenticator = new Authenticator[1];

                        webAuthn.authenticatorUpdater(
                            authenticator -> {
                              if (authenticator == null) {
                                log.error("Authenticator is null in authenticatorUpdater");
                                return Future.failedFuture(
                                    new RuntimeException(
                                        "Authenticator is null - verification may have failed"));
                              }
                              storedAuthenticator[0] = authenticator;
                              return completableToFuture(
                                  saveNewCredentialFromAuthenticator(
                                      authenticator, ctx, tenantId, requestDto));
                            });

                        return Single.fromCompletionStage(
                                webAuthn.authenticate(authRequest).toCompletionStage())
                            .flatMap(
                                user -> {
                                  return validateWebAuthnRequirements(
                                          user,
                                          config,
                                          requestDto,
                                          null,
                                          WEBAUTHN_STATE_TYPE_ENROLL,
                                          storedAuthenticator[0])
                                      .andThen(Single.just(ctx));
                                })
                            .onErrorResumeNext(
                                err ->
                                    handleEnrollmentError(
                                        err,
                                        ctx,
                                        requestDto,
                                        config,
                                        tenantId,
                                        state,
                                        headers,
                                        credentialId));
                      });
            });
  }

  /**
   * Verifies assertion by fetching credential from DB, then verifying challenge with public key.
   *
   * @param context the finish context with config
   * @param requestDto the finish request DTO
   * @param tenantId the tenant identifier
   * @return Single containing the verified context
   */
  private Single<FinishContextWithConfig> verifyAssertion(
      FinishContextWithConfig context, V2WebAuthnFinishRequestDto requestDto, String tenantId) {
    WebAuthnConfigModel config = context.getWebAuthnContext().getConfig();
    WebAuthnStateModel state = context.getState();

    return credentialDao
        .getCredentialById(
            tenantId,
            requestDto.getClientId(),
            context.getTokenContext().getUserId(),
            requestDto.getCredential().getId())
        .switchIfEmpty(
            Single.error(INVALID_REQUEST.getCustomException(WEBAUTHN_ERROR_CREDENTIAL_NOT_FOUND)))
        .flatMap(
            credential -> {
              WebAuthn webAuthn = createWebAuthnInstance(config);

              long storedSignCount =
                  credential.getSignCount() != null ? credential.getSignCount() : 0L;

              return buildAuthRequest(context, state, requestDto, config)
                  .flatMap(
                      authRequest -> {
                        webAuthn.authenticatorFetcher(
                            query -> {
                              List<Authenticator> authenticators = new ArrayList<>();
                              Authenticator authenticator =
                                  buildAuthenticatorFromCredential(credential);
                              authenticators.add(authenticator);
                              return Future.succeededFuture(authenticators);
                            });

                        webAuthn.authenticatorUpdater(
                            authenticator -> {
                              Long newSignCount = authenticator.getCounter();
                              if (newSignCount != null) {
                                if (storedSignCount == 0L && newSignCount == 0L) {
                                  log.debug(
                                      "Allowing sign count 0->0 transition (first use). Credential ID: {}",
                                      requestDto.getCredential().getId());
                                } else if (newSignCount <= storedSignCount) {
                                  log.error(
                                      "Sign count validation failed: stored={}, new={}, credentialId={}",
                                      storedSignCount,
                                      newSignCount,
                                      requestDto.getCredential().getId());
                                  return Future.failedFuture(
                                      new RuntimeException(WEBAUTHN_ERROR_SIGN_COUNT_REPLAY));
                                }
                              }
                              return completableToFuture(
                                  updateSignCount(
                                      tenantId,
                                      requestDto.getClientId(),
                                      context.getTokenContext().getUserId(),
                                      requestDto.getCredential().getId(),
                                      newSignCount));
                            });

                        return Single.fromCompletionStage(
                                webAuthn.authenticate(authRequest).toCompletionStage())
                            .flatMap(
                                user -> {
                                  return validateWebAuthnRequirements(
                                          user,
                                          config,
                                          requestDto,
                                          credential,
                                          WEBAUTHN_STATE_TYPE_ASSERT,
                                          null)
                                      .andThen(Single.just(context));
                                })
                            .onErrorResumeNext(
                                err -> {
                                  log.error("WebAuthn assertion verification failed", err);
                                  String errorMessage = err.getMessage();
                                  if (errorMessage != null) {
                                    log.error("Error details: {}", errorMessage);
                                  }
                                  return Single.error(
                                      INVALID_REQUEST.getCustomException(
                                          WEBAUTHN_ERROR_VERIFICATION_FAILED
                                              + (errorMessage != null ? ": " + errorMessage : "")));
                                });
                      });
            });
  }

  /**
   * Converts Completable to Future for use with WebAuthn authenticator updater.
   *
   * @param completable the Completable to convert
   * @return Future that completes when the Completable completes
   */
  private Future<Void> completableToFuture(Completable completable) {
    Promise<Void> promise = Promise.promise();
    completable.subscribe(() -> promise.complete(), promise::fail);
    return promise.future();
  }

  /**
   * Creates a WebAuthn instance with relying party configuration.
   *
   * @param config the WebAuthn configuration
   * @return configured WebAuthn instance
   */
  private WebAuthn createWebAuthnInstance(WebAuthnConfigModel config) {
    WebAuthnOptions webAuthnOptions =
        new WebAuthnOptions()
            .setRelyingParty(new RelyingParty().setName(config.getRpId()).setId(config.getRpId()));
    return WebAuthn.create(vertx.getDelegate(), webAuthnOptions);
  }

  /**
   * Builds authentication request JSON for WebAuthn verification and validates origin.
   *
   * @param context the finish context with config
   * @param state the WebAuthn state
   * @param requestDto the finish request DTO
   * @param config the WebAuthn configuration
   * @return Single containing the authentication request JSON
   */
  private Single<JsonObject> buildAuthRequest(
      FinishContextWithConfig context,
      WebAuthnStateModel state,
      V2WebAuthnFinishRequestDto requestDto,
      WebAuthnConfigModel config) {
    JsonObject credentialJson = buildCredentialJson(requestDto, config);

    String origin = extractOriginFromClientData(requestDto);
    if (origin == null) {
      origin = getOriginFromConfig(config);
    } else {
      try {
        validateOrigin(origin, config);
      } catch (IllegalArgumentException e) {
        return Single.error(INVALID_REQUEST.getCustomException(e.getMessage()));
      }
    }

    String domain = config.getRpId();

    JsonObject authRequest = new JsonObject();
    authRequest.put(WEBAUTHN_JSON_KEY_USERNAME, context.getTokenContext().getUserId());
    authRequest.put(WEBAUTHN_JSON_KEY_CHALLENGE, state.getChallenge());
    authRequest.put(WEBAUTHN_JSON_KEY_WEBAUTHN, credentialJson);
    authRequest.put(WEBAUTHN_JSON_KEY_ORIGIN, origin);
    authRequest.put(WEBAUTHN_JSON_KEY_DOMAIN, domain);
    return Single.just(authRequest);
  }

  /**
   * Extracts origin from clientDataJSON in the credential response.
   *
   * @param requestDto the finish request DTO
   * @return the origin string, or null if extraction fails
   */
  private String extractOriginFromClientData(V2WebAuthnFinishRequestDto requestDto) {
    try {
      String clientDataJSON = requestDto.getCredential().getResponse().getClientDataJSON();

      if (clientDataJSON != null && !clientDataJSON.isEmpty()) {
        byte[] decoded = Base64.getUrlDecoder().decode(clientDataJSON);
        JsonObject clientData = new JsonObject(new String(decoded));
        return clientData.getString("origin");
      }
    } catch (Exception e) {
      log.warn("Failed to extract origin from clientDataJSON", e);
    }
    return null;
  }

  /**
   * Validates origin against allowed origins from WebAuthn configuration.
   *
   * @param origin the origin to validate
   * @param config the WebAuthn configuration
   * @throws IllegalArgumentException if the origin is not allowed
   */
  private void validateOrigin(String origin, WebAuthnConfigModel config) {
    if (config.getAllowedWebOrigins() != null && !config.getAllowedWebOrigins().isEmpty()) {
      if (!config.getAllowedWebOrigins().contains(origin)) {
        throw new IllegalArgumentException("Origin not allowed: " + origin);
      }
    }
  }

  /**
   * Builds an Authenticator object from a CredentialModel.
   *
   * @param credential the credential model to convert
   * @return the Authenticator object
   */
  private Authenticator buildAuthenticatorFromCredential(CredentialModel credential) {
    Authenticator authenticator = new Authenticator();
    authenticator.setCredID(credential.getCredentialId());
    authenticator.setPublicKey(credential.getPublicKey());
    authenticator.setCounter(credential.getSignCount() != null ? credential.getSignCount() : 0L);
    return authenticator;
  }

  /**
   * Builds credential JSON from request DTO for WebAuthn verification.
   *
   * <p>Validates required fields and constructs the credential JSON structure. For enrollment
   * response (attestation): includes attestation object and client data JSON. Workaround: Fixes
   * fmt="none" with non-zero AAGUID for platform authenticators. For assertion response
   * (authentication): includes authenticator data, client data JSON, and signature.
   *
   * @param requestDto the finish request DTO
   * @param config the WebAuthn configuration
   * @return the credential JSON object
   * @throws IllegalArgumentException if required fields are missing
   */
  private JsonObject buildCredentialJson(
      V2WebAuthnFinishRequestDto requestDto, WebAuthnConfigModel config) {
    if (requestDto.getCredential() == null) {
      throw new IllegalArgumentException("Credential cannot be null");
    }
    if (requestDto.getCredential().getId() == null
        || requestDto.getCredential().getId().isEmpty()) {
      throw new IllegalArgumentException("Credential ID cannot be null or empty");
    }
    if (requestDto.getCredential().getType() == null
        || requestDto.getCredential().getType().isEmpty()) {
      throw new IllegalArgumentException("Credential type cannot be null or empty");
    }
    if (requestDto.getCredential().getResponse() == null) {
      throw new IllegalArgumentException("Credential response cannot be null");
    }

    JsonObject credential = new JsonObject();
    credential.put(WEBAUTHN_JSON_KEY_ID, requestDto.getCredential().getId());
    credential.put(WEBAUTHN_JSON_KEY_RAW_ID, requestDto.getCredential().getId());
    credential.put(WEBAUTHN_JSON_KEY_TYPE, requestDto.getCredential().getType());

    JsonObject response = new JsonObject();
    V2WebAuthnFinishRequestDto.ResponseDto responseDto = requestDto.getCredential().getResponse();

    if (responseDto.getAttestationObject() != null) {
      if (responseDto.getClientDataJSON() == null || responseDto.getClientDataJSON().isEmpty()) {
        throw new IllegalArgumentException("clientDataJSON is required for enrollment");
      }
      String attestationObject =
          fixAttestationObjectForPlatformAuthenticators(responseDto.getAttestationObject(), config);
      response.put(WEBAUTHN_JSON_KEY_ATTESTATION_OBJECT, attestationObject);
      response.put(WEBAUTHN_JSON_KEY_CLIENT_DATA_JSON, responseDto.getClientDataJSON());
    } else {
      if (responseDto.getAuthenticatorData() == null
          || responseDto.getAuthenticatorData().isEmpty()) {
        throw new IllegalArgumentException("authenticatorData is required for assertion");
      }
      if (responseDto.getClientDataJSON() == null || responseDto.getClientDataJSON().isEmpty()) {
        throw new IllegalArgumentException("clientDataJSON is required for assertion");
      }
      if (responseDto.getSignature() == null || responseDto.getSignature().isEmpty()) {
        throw new IllegalArgumentException("signature is required for assertion");
      }
      response.put(WEBAUTHN_JSON_KEY_AUTHENTICATOR_DATA, responseDto.getAuthenticatorData());
      response.put(WEBAUTHN_JSON_KEY_CLIENT_DATA_JSON, responseDto.getClientDataJSON());
      response.put(WEBAUTHN_JSON_KEY_SIGNATURE, responseDto.getSignature());
      if (responseDto.getUserHandle() != null) {
        response.put(WEBAUTHN_JSON_KEY_USER_HANDLE, responseDto.getUserHandle());
      }
    }

    credential.put(WEBAUTHN_JSON_KEY_RESPONSE, response);
    return credential;
  }

  /**
   * Workaround placeholder for platform authenticators that return fmt="none" with non-zero AAGUID.
   *
   * <p>Note: The AAGUID is part of the signed authenticatorData, so we cannot modify it without
   * breaking signature verification. The Vert.x WebAuthn library validates this before we can
   * intercept it. This is a known limitation with platform authenticators (Touch ID, Face ID).
   *
   * <p>Possible solutions: 1. Use a different authenticator (security key) that complies with the
   * spec 2. Upgrade to a newer version of vertx-auth-webauthn that handles this more leniently 3.
   * Implement custom attestation validation (complex, requires full CBOR parsing and signature
   * verification)
   *
   * <p>For now, this method returns the attestation object as-is. The error will be caught and
   * handled with a helpful error message.
   */
  private String fixAttestationObjectForPlatformAuthenticators(
      String attestationObjectBase64, WebAuthnConfigModel config) {
    return attestationObjectBase64;
  }

  /**
   * Validates WebAuthn requirements: UV flag, transports, AAGUID, etc. For enrollment, checks
   * requireUvEnrollment; for assertion, checks requireUvAuth.
   *
   * <p>Gets authenticator data from user principal. If WebAuthn data is not available in principal,
   * tries alternative key names or extracts from Authenticator object or attestation object
   * directly. If WebAuthn data is still not available, logs warning but continues with lenient
   * validation (creates empty webauthn object to avoid NPE, but skips strict validation).
   *
   * <p>For User Verification (UV) flag: checks if UV is required based on state type. If webauthn
   * data is empty and UV validation is required, logs warning but doesn't fail (workaround for
   * library versions that don't populate webauthn data).
   *
   * <p>Validates transports if required and available. If transport is null but webauthn data is
   * empty, skips validation.
   *
   * <p>For enrollment, validates AAGUID if configured: checks if AAGUID is blocked or if it's in
   * allowlist (if allowlist mode is enabled).
   *
   * @param user the authenticated user from WebAuthn verification
   * @param config the WebAuthn configuration
   * @param requestDto the finish request DTO
   * @param credential the credential model (null for enrollment)
   * @param stateType the state type (enrollment or assertion)
   * @param authenticator the authenticator object (null for assertion)
   * @return Completable that completes if validation passes, or errors if validation fails
   */
  private Completable validateWebAuthnRequirements(
      io.vertx.ext.auth.User user,
      WebAuthnConfigModel config,
      V2WebAuthnFinishRequestDto requestDto,
      CredentialModel credential,
      String stateType,
      Authenticator authenticator) {
    // Get authenticator data from user principal
    JsonObject principal = user.principal();

    // Log principal structure for debugging
    log.debug("User principal keys: {}", principal.fieldNames());
    log.debug("User principal: {}", principal.encodePrettily());

    JsonObject webauthn = principal.getJsonObject(WEBAUTHN_JSON_KEY_WEBAUTHN);

    if (webauthn == null) {
      // Try alternative key names that the library might use
      webauthn = principal.getJsonObject("webauthn");
      if (webauthn == null) {
        webauthn = principal.getJsonObject("webauthn_data");
      }
      if (webauthn == null && authenticator != null) {
        log.debug(
            "WebAuthn data not in principal, attempting to extract from Authenticator or attestation object");
        webauthn = extractWebAuthnDataFromAuthenticator(authenticator, requestDto);
      }
      if (webauthn == null) {
        log.warn(
            "WebAuthn data not found in user principal. Principal keys: {}. "
                + "Skipping UV/transport validation. Principal structure: {}",
            principal.fieldNames(),
            principal.encodePrettily());
        webauthn = new JsonObject();
      }
    }

    boolean requireUv = false;
    if (WEBAUTHN_STATE_TYPE_ENROLL.equals(stateType)) {
      requireUv = config.getRequireUvEnrollment() != null && config.getRequireUvEnrollment();
    } else if (WEBAUTHN_STATE_TYPE_ASSERT.equals(stateType)) {
      requireUv = config.getRequireUvAuth() != null && config.getRequireUvAuth();
    }

    if (requireUv) {
      Boolean uv = webauthn.getBoolean(WEBAUTHN_JSON_KEY_UV);
      if (uv == null || !uv) {
        if (webauthn.isEmpty()) {
          log.warn(
              "UV validation required but webauthn data not available in principal. "
                  + "Skipping UV check. Consider extracting UV from attestation object directly.");
        } else {
          return Completable.error(
              INVALID_REQUEST.getCustomException(WEBAUTHN_ERROR_USER_VERIFICATION_REQUIRED));
        }
      }
    }

    if (config.getAllowedTransports() != null && !config.getAllowedTransports().isEmpty()) {
      String transport = webauthn.getString(WEBAUTHN_JSON_KEY_TRANSPORT);
      if (transport != null && !config.getAllowedTransports().contains(transport)) {
        return Completable.error(
            INVALID_REQUEST.getCustomException(WEBAUTHN_ERROR_INVALID_TRANSPORT));
      }
      if (transport == null && webauthn.isEmpty()) {
        log.debug("Transport validation skipped - webauthn data not available in principal");
      }
    }

    if (WEBAUTHN_STATE_TYPE_ENROLL.equals(stateType)) {
      String aaguid = webauthn.getString(WEBAUTHN_JSON_KEY_AAGUID);
      if (aaguid != null && !aaguid.isEmpty()) {
        if (config.getBlockedAaguids() != null && config.getBlockedAaguids().contains(aaguid)) {
          return Completable.error(
              INVALID_REQUEST.getCustomException("AAGUID is blocked: " + aaguid));
        }
        if ("allowlist".equals(config.getAaguidPolicyMode())
            && config.getAllowedAaguids() != null
            && !config.getAllowedAaguids().isEmpty()
            && !config.getAllowedAaguids().contains(aaguid)) {
          return Completable.error(
              INVALID_REQUEST.getCustomException("AAGUID not in allowlist: " + aaguid));
        }
      }
    }

    return Completable.complete();
  }

  /**
   * Extract WebAuthn data (UV, transport, AAGUID) from Authenticator object or attestation object.
   * This is a fallback when the library doesn't populate webauthn data in the user principal.
   */
  private JsonObject extractWebAuthnDataFromAuthenticator(
      Authenticator authenticator, V2WebAuthnFinishRequestDto requestDto) {
    JsonObject webauthnData = new JsonObject();

    try {
      V2WebAuthnFinishRequestDto.ResponseDto response = requestDto.getCredential().getResponse();

      // For enrollment, extract from attestation object
      if (response.getAttestationObject() != null) {
        byte[] authenticatorData =
            extractAuthenticatorDataFromAttestationObject(response.getAttestationObject());
        if (authenticatorData != null) {
          extractDataFromAuthenticatorData(authenticatorData, webauthnData);
        }
        // Extract PIN information from attestation object
        extractPinFromAttestationObject(response.getAttestationObject(), webauthnData);
      }

      // For assertion, extract from authenticatorData directly
      if (response.getAuthenticatorData() != null) {
        byte[] authenticatorDataBytes =
            Base64.getUrlDecoder().decode(response.getAuthenticatorData());
        extractDataFromAuthenticatorData(authenticatorDataBytes, webauthnData);
      }

      log.debug("Extracted WebAuthn data: {}", webauthnData.encodePrettily());
      return webauthnData.isEmpty() ? null : webauthnData;
    } catch (Exception e) {
      log.warn("Failed to extract WebAuthn data from authenticator", e);
      return null;
    }
  }

  /**
   * Extracts PIN information from attestation object.
   *
   * <p>PIN detection: Checks if user verification was performed and if PIN was the method used.
   * This is typically indicated in the COSE key extensions or can be inferred from UV flag.
   *
   * <p>Extracts "authData" to check UV flag. If UV is true, checks if PIN was used. Note: PIN
   * detection in WebAuthn is complex - the UV flag indicates user verification was performed, but
   * doesn't specify the method. For now, uses a heuristic: if UV is true, assumes PIN might have
   * been used (this can be refined based on authenticator type or additional attestation statement
   * parsing).
   *
   * <p>Sets PIN flag if detected - this will be used to determine AMR format. Actual PIN detection
   * may require parsing COSE key extensions or attestation statement format-specific fields, which
   * can be added here if needed.
   *
   * @param attestationObjectBase64 the base64-encoded attestation object
   * @param webauthnData the JSON object to populate with extracted data
   */
  private void extractPinFromAttestationObject(
      String attestationObjectBase64, JsonObject webauthnData) {
    try {
      byte[] attestationObjectBytes = Base64.getUrlDecoder().decode(attestationObjectBase64);
      ByteArrayInputStream bais = new ByteArrayInputStream(attestationObjectBytes);
      CborDecoder decoder = new CborDecoder(bais);
      List<DataItem> dataItems = decoder.decode();

      if (dataItems.isEmpty()) {
        return;
      }

      DataItem item = dataItems.get(0);
      if (!(item instanceof co.nstant.in.cbor.model.Map)) {
        return;
      }

      co.nstant.in.cbor.model.Map cborMap = (co.nstant.in.cbor.model.Map) item;

      DataItem authDataItem = cborMap.get(new UnicodeString("authData"));
      if (authDataItem instanceof ByteString) {
        ByteString authDataByteString = (ByteString) authDataItem;
        byte[] authData = authDataByteString.getBytes();

        if (authData.length >= 37) {
          byte flags = authData[32];
          boolean uv = (flags & 0x04) != 0;

          if (uv) {
            webauthnData.put("pin", true);
            log.debug("PIN detected from attestation object (UV flag set)");
          }
        }
      }
    } catch (Exception e) {
      log.debug("Failed to extract PIN from attestation object", e);
    }
  }

  /**
   * Extract authenticatorData from attestation object (CBOR format). The attestation object is a
   * CBOR map with keys: "fmt", "attStmt", "authData"
   */
  private byte[] extractAuthenticatorDataFromAttestationObject(String attestationObjectBase64) {
    try {
      byte[] attestationObjectBytes = Base64.getUrlDecoder().decode(attestationObjectBase64);
      ByteArrayInputStream bais = new ByteArrayInputStream(attestationObjectBytes);
      CborDecoder decoder = new CborDecoder(bais);
      List<DataItem> dataItems = decoder.decode();

      if (dataItems.isEmpty()) {
        log.warn("Attestation object CBOR decoding returned empty result");
        return null;
      }

      DataItem item = dataItems.get(0);
      if (!(item instanceof co.nstant.in.cbor.model.Map)) {
        log.warn("Attestation object is not a CBOR map");
        return null;
      }

      co.nstant.in.cbor.model.Map cborMap = (co.nstant.in.cbor.model.Map) item;

      DataItem authDataItem = cborMap.get(new UnicodeString("authData"));
      if (authDataItem == null) {
        log.warn("authData not found in attestation object");
        return null;
      }

      if (!(authDataItem instanceof ByteString)) {
        log.warn("authData is not a byte string in attestation object");
        return null;
      }

      ByteString authDataByteString = (ByteString) authDataItem;
      return authDataByteString.getBytes();
    } catch (CborException e) {
      log.warn("Failed to parse attestation object as CBOR", e);
      return null;
    } catch (Exception e) {
      log.warn("Failed to extract authenticatorData from attestation object", e);
      return null;
    }
  }

  /**
   * Extract UV flag and AAGUID from authenticatorData binary structure. Structure: - rpIdHash: 32
   * bytes - flags: 1 byte (bit 2 = UV, bit 0 = UP) - signCount: 4 bytes (big-endian) -
   * attestedCredentialData (if present): - AAGUID: 16 bytes - credentialIdLength: 2 bytes
   * (big-endian) - credentialId: variable length - credentialPublicKey: CBOR-encoded COSE key
   */
  private void extractDataFromAuthenticatorData(byte[] authenticatorData, JsonObject webauthnData) {
    if (authenticatorData == null || authenticatorData.length < 37) {
      log.warn(
          "AuthenticatorData too short: {} bytes",
          authenticatorData != null ? authenticatorData.length : 0);
      return;
    }

    try {
      ByteBuffer buffer = ByteBuffer.wrap(authenticatorData).order(ByteOrder.BIG_ENDIAN);

      buffer.position(32);

      byte flags = buffer.get();

      boolean uv = (flags & 0x04) != 0;
      webauthnData.put(WEBAUTHN_JSON_KEY_UV, uv);

      buffer.getInt();

      boolean attestedCredentialDataPresent = (flags & 0x40) != 0;

      if (attestedCredentialDataPresent && buffer.remaining() >= 18) {
        long mostSignificantBits = buffer.getLong();
        long leastSignificantBits = buffer.getLong();
        UUID aaguid = new UUID(mostSignificantBits, leastSignificantBits);
        webauthnData.put("aaguid", aaguid.toString());

        log.debug("Extracted AAGUID: {}, UV: {}", aaguid, uv);
      } else {
        log.debug("No attestedCredentialData present or insufficient data. UV: {}", uv);
      }
    } catch (Exception e) {
      log.warn("Failed to parse authenticatorData", e);
    }
  }

  /**
   * Bypass library's AAGUID validation and perform minimal custom validation for platform
   * authenticators. This is a workaround when AAGUID policy is "any" with no restrictions.
   *
   * <p>Note: This performs minimal validation (challenge, origin, type) but skips full signature
   * verification. The public key will be extracted from the attestation object by the library on
   * subsequent authentications.
   */
  private Single<FinishContextWithConfig> bypassAaguidValidationAndEnroll(
      FinishContextWithConfig context,
      V2WebAuthnFinishRequestDto requestDto,
      WebAuthnConfigModel config,
      String tenantId,
      WebAuthnStateModel state,
      HttpHeaders headers) {
    try {
      // Extract basic info from request
      String credentialId = requestDto.getCredential().getId();
      String clientDataJSON = requestDto.getCredential().getResponse().getClientDataJSON();
      String attestationObjectBase64 =
          requestDto.getCredential().getResponse().getAttestationObject();

      // Validate required fields
      if (clientDataJSON == null || clientDataJSON.isEmpty()) {
        return Single.error(INVALID_REQUEST.getCustomException("clientDataJSON is required"));
      }
      if (attestationObjectBase64 == null || attestationObjectBase64.isEmpty()) {
        return Single.error(INVALID_REQUEST.getCustomException("attestationObject is required"));
      }

      // Decode and validate clientDataJSON
      byte[] clientDataBytes = Base64.getUrlDecoder().decode(clientDataJSON);
      JsonObject clientData = new JsonObject(new String(clientDataBytes));

      // Verify challenge matches
      String challenge = clientData.getString("challenge");
      if (challenge == null || !state.getChallenge().equals(challenge)) {
        return Single.error(INVALID_REQUEST.getCustomException("Challenge mismatch"));
      }

      // Verify origin
      String origin = clientData.getString("origin");
      if (origin != null) {
        try {
          validateOrigin(origin, config);
        } catch (IllegalArgumentException e) {
          return Single.error(INVALID_REQUEST.getCustomException(e.getMessage()));
        }
      }

      // Verify type
      String type = clientData.getString(WEBAUTHN_JSON_KEY_TYPE);
      if (!WEBAUTHN_CLIENT_DATA_TYPE_CREATE.equals(type)) {
        return Single.error(INVALID_REQUEST.getCustomException("Invalid clientData type: " + type));
      }

      log.info(
          "AAGUID check disabled - bypassing library validation for platform authenticator. "
              + "Performing minimal validation. Credential ID: {}",
          credentialId);

      String aaguid = null;
      CredentialModel credential =
          CredentialModel.builder()
              .tenantId(tenantId)
              .clientId(requestDto.getClientId())
              .userId(context.getTokenContext().getUserId())
              .credentialId(credentialId)
              .publicKey("") // Placeholder - will be extracted on first authentication
              .bindingType(WEBAUTHN_BINDING_TYPE_WEBAUTHN)
              .alg(COSE_ALG_ES256) // Default, will be determined from attestation on first use
              .signCount(0L)
              .aaguid(aaguid)
              .build();

      log.warn(
          "Saving credential with placeholder public key. "
              + "Public key will be extracted on first authentication. Credential ID: {}",
          credentialId);

      return credentialDao.saveCredential(credential).andThen(Single.just(context));

    } catch (Exception e) {
      log.error("Error in bypassAaguidValidationAndEnroll", e);
      return Single.error(
          INVALID_REQUEST.getCustomException(
              "Error bypassing AAGUID validation: " + e.getMessage()));
    }
  }

  /**
   * Updates the refresh token's authentication method (AMR) with WebAuthn.
   *
   * <p>Stores the original enum values directly (e.g., "hwk", "pwd") without transformation.
   * Removes duplicates to ensure same value is not put in AMR.
   *
   * @param tenantId the tenant identifier
   * @param clientId the client identifier
   * @param refreshToken the refresh token string
   * @param authMethods the list of authentication methods to update
   * @return Completable that completes when the update is done
   */
  private Completable updateRefreshTokenAuthMethod(
      String tenantId, String clientId, String refreshToken, List<AuthMethod> authMethods) {
    List<String> authMethodValues =
        authMethods.stream().map(AuthMethod::getValue).distinct().toList();
    return refreshTokenDao.updateRefreshTokenAuthMethod(
        tenantId, clientId, refreshToken, authMethodValues);
  }

  /**
   * Gets the origin from WebAuthn configuration. Uses the first allowed origin if available,
   * otherwise constructs from rpId.
   *
   * @param config the WebAuthn configuration
   * @return the origin string
   */
  private String getOriginFromConfig(WebAuthnConfigModel config) {
    if (config.getAllowedWebOrigins() != null && !config.getAllowedWebOrigins().isEmpty()) {
      return config.getAllowedWebOrigins().get(0);
    }
    return "https://" + config.getRpId();
  }

  /**
   * Updates the sign count for a WebAuthn credential.
   *
   * @param tenantId the tenant identifier
   * @param clientId the client identifier
   * @param userId the user identifier
   * @param credentialId the credential identifier
   * @param signCount the new sign count value
   * @return Completable that completes when the update is done
   */
  private Completable updateSignCount(
      String tenantId, String clientId, String userId, String credentialId, Long signCount) {
    return credentialDao.updateSignCount(tenantId, clientId, userId, credentialId, signCount);
  }

  /**
   * Saves a new WebAuthn credential from the authenticator after successful enrollment.
   *
   * @param authenticator the authenticator object containing credential information
   * @param context the finish context with config
   * @param tenantId the tenant identifier
   * @param requestDto the finish request DTO
   * @return Completable that completes when the credential is saved
   */
  private Completable saveNewCredentialFromAuthenticator(
      Authenticator authenticator,
      FinishContextWithConfig context,
      String tenantId,
      V2WebAuthnFinishRequestDto requestDto) {
    if (authenticator == null) {
      log.error("Authenticator is null in saveNewCredentialFromAuthenticator");
      return Completable.error(new RuntimeException("Authenticator is null"));
    }

    String credentialId = requestDto.getCredential().getId();
    String publicKey = authenticator.getPublicKey();
    if (publicKey == null || publicKey.isEmpty()) {
      log.error("Public key is null or empty in authenticator");
      return Completable.error(new RuntimeException("Public key is null or empty"));
    }

    Long counter = authenticator.getCounter();
    String aaguid = authenticator.getAaguid();

    Integer alg = COSE_ALG_ES256;

    CredentialModel credential =
        CredentialModel.builder()
            .tenantId(tenantId)
            .clientId(requestDto.getClientId())
            .userId(context.getTokenContext().getUserId())
            .credentialId(credentialId)
            .publicKey(publicKey)
            .bindingType(WEBAUTHN_BINDING_TYPE_WEBAUTHN)
            .alg(alg)
            .signCount(counter != null ? counter : 0L)
            .aaguid(aaguid)
            .build();

    return credentialDao.saveCredential(credential);
  }

  /**
   * Processes the WebAuthn result after successful verification.
   *
   * <p>Gets user information and builds authentication methods list, adding WebAuthn (hwk) if not
   * already present.
   *
   * <p>For enrollment: updates existing refresh token AMR, then issues access token and ID token.
   * For assertion: updates refresh token AMR and issues new access token (returns the same refresh
   * token).
   *
   * <p>Note: WebAuthn requirements (UV flag, transports, AAGUID) are validated earlier in the flow
   * during verification, so no additional validation is needed here.
   *
   * @param context the finish context with config
   * @param requestDto the finish request DTO
   * @param tenantId the tenant identifier
   * @param requestHeaders the HTTP request headers
   * @return Single containing the response map with access token, refresh token, and optionally ID
   *     token
   */
  private Single<Map<String, Object>> processWebAuthnResult(
      FinishContextWithConfig context,
      V2WebAuthnFinishRequestDto requestDto,
      String tenantId,
      MultivaluedMap<String, String> requestHeaders) {
    WebAuthnStateModel state = context.getState();
    RefreshTokenContext tokenContext = context.getTokenContext();
    TenantConfig config = registry.get(tenantId, TenantConfig.class);

    return userService
        .getUser(Map.of("userId", tokenContext.getUserId()), requestHeaders, tenantId)
        .flatMap(
            user -> {
              RefreshTokenModel refreshToken = tokenContext.getRefreshToken();
              String scopes = buildScopesFromRefreshToken(refreshToken);
              List<AuthMethod> authMethods = buildAuthMethodsWithWebAuthn(refreshToken);
              long iat = getCurrentTimeInSeconds();

              if (WEBAUTHN_STATE_TYPE_ENROLL.equals(state.getType())) {
                return processEnrollmentResult(
                    requestDto, tenantId, config, refreshToken, user, authMethods, scopes, iat);
              } else {
                return processAssertionResult(
                    requestDto, tenantId, config, refreshToken, user, authMethods, scopes, iat);
              }
            });
  }

  /**
   * Builds scopes string from refresh token.
   *
   * @param refreshToken the refresh token model
   * @return the scopes string, empty if no scopes
   */
  private String buildScopesFromRefreshToken(RefreshTokenModel refreshToken) {
    return refreshToken.getScope() != null ? String.join(" ", refreshToken.getScope()) : "";
  }

  /**
   * Builds authentication methods list with WebAuthn added if not already present.
   *
   * @param refreshToken the refresh token model
   * @return the list of authentication methods including WebAuthn
   */
  private List<AuthMethod> buildAuthMethodsWithWebAuthn(RefreshTokenModel refreshToken) {
    List<AuthMethod> authMethods = new ArrayList<>(refreshToken.getAuthMethod());
    if (!authMethods.contains(AuthMethod.HARDWARE_KEY_PROOF)) {
      authMethods.add(AuthMethod.HARDWARE_KEY_PROOF);
    }
    return authMethods;
  }

  /**
   * Processes enrollment result: updates refresh token AMR and issues access token and ID token.
   *
   * @param requestDto the finish request DTO
   * @param tenantId the tenant identifier
   * @param config the tenant configuration
   * @param refreshToken the refresh token model
   * @param user the user JSON object
   * @param authMethods the list of authentication methods
   * @param scopes the scopes string
   * @param iat the issued at time in seconds
   * @return Single containing the response map with access token, refresh token, and ID token
   */
  private Single<Map<String, Object>> processEnrollmentResult(
      V2WebAuthnFinishRequestDto requestDto,
      String tenantId,
      TenantConfig config,
      RefreshTokenModel refreshToken,
      JsonObject user,
      List<AuthMethod> authMethods,
      String scopes,
      long iat) {
    String existingRefreshToken = refreshToken.getRefreshToken();

    return updateRefreshTokenAuthMethod(
            tenantId, requestDto.getClientId(), existingRefreshToken, authMethods)
        .andThen(
            Single.zip(
                tokenIssuer.generateAccessToken(
                    existingRefreshToken,
                    iat,
                    scopes,
                    user,
                    authMethods,
                    requestDto.getClientId(),
                    tenantId,
                    config),
                tokenIssuer.generateIdToken(
                    iat,
                    null,
                    user,
                    config.getTokenConfig().getIdTokenClaims(),
                    requestDto.getClientId(),
                    config.getTenantId()),
                (accessToken, idToken) ->
                    buildEnrollmentResponse(accessToken, existingRefreshToken, idToken, config)));
  }

  /**
   * Processes assertion result: updates refresh token AMR and issues new access token.
   *
   * @param requestDto the finish request DTO
   * @param tenantId the tenant identifier
   * @param config the tenant configuration
   * @param refreshToken the refresh token model
   * @param user the user JSON object
   * @param authMethods the list of authentication methods
   * @param scopes the scopes string
   * @param iat the issued at time in seconds
   * @return Single containing the response map with access token and refresh token
   */
  private Single<Map<String, Object>> processAssertionResult(
      V2WebAuthnFinishRequestDto requestDto,
      String tenantId,
      TenantConfig config,
      RefreshTokenModel refreshToken,
      JsonObject user,
      List<AuthMethod> authMethods,
      String scopes,
      long iat) {
    String existingRefreshToken = refreshToken.getRefreshToken();

    return updateRefreshTokenAuthMethod(
            tenantId, requestDto.getClientId(), existingRefreshToken, authMethods)
        .andThen(
            tokenIssuer.generateAccessToken(
                existingRefreshToken,
                iat,
                scopes,
                user,
                authMethods,
                requestDto.getClientId(),
                tenantId,
                config))
        .map(accessToken -> buildAssertionResponse(accessToken, existingRefreshToken, config));
  }

  /**
   * Builds enrollment response map with access token, refresh token, ID token, and metadata.
   *
   * @param accessToken the access token
   * @param refreshToken the refresh token
   * @param idToken the ID token
   * @param config the tenant configuration
   * @return the response map
   */
  private Map<String, Object> buildEnrollmentResponse(
      String accessToken, String refreshToken, String idToken, TenantConfig config) {
    Map<String, Object> response = new HashMap<>();
    response.put(WEBAUTHN_JSON_KEY_ACCESS_TOKEN, accessToken);
    response.put(WEBAUTHN_JSON_KEY_REFRESH_TOKEN, refreshToken);
    response.put(WEBAUTHN_JSON_KEY_ID_TOKEN, idToken);
    response.put(WEBAUTHN_JSON_KEY_TOKEN_TYPE, TOKEN_TYPE);
    response.put(WEBAUTHN_JSON_KEY_EXPIRES_IN, config.getTokenConfig().getAccessTokenExpiry());
    return response;
  }

  /**
   * Builds assertion response map with access token, refresh token, and metadata.
   *
   * @param accessToken the access token
   * @param refreshToken the refresh token
   * @param config the tenant configuration
   * @return the response map
   */
  private Map<String, Object> buildAssertionResponse(
      String accessToken, String refreshToken, TenantConfig config) {
    Map<String, Object> response = new HashMap<>();
    response.put(WEBAUTHN_JSON_KEY_ACCESS_TOKEN, accessToken);
    response.put(WEBAUTHN_JSON_KEY_REFRESH_TOKEN, refreshToken);
    response.put(WEBAUTHN_JSON_KEY_TOKEN_TYPE, TOKEN_TYPE);
    response.put(WEBAUTHN_JSON_KEY_EXPIRES_IN, config.getTokenConfig().getAccessTokenExpiry());
    return response;
  }

  // Helper classes for context passing
  @Builder
  @Getter
  private static class RefreshTokenContext {
    private String userId;
    private String clientId;
    private RefreshTokenModel refreshToken;
  }

  @Builder
  @Getter
  private static class WebAuthnContext {
    private WebAuthnConfigModel config;
    private List<CredentialModel> credentials;
    private String userId;
    private String clientId;
  }

  @Builder
  @Getter
  private static class FinishContext {
    private RefreshTokenContext tokenContext;
    private WebAuthnStateModel state;
    private ClientModel client;
  }

  @Builder
  @Getter
  private static class FinishContextWithConfig {
    private RefreshTokenContext tokenContext;
    private WebAuthnStateModel state;
    private ClientModel client;
    private WebAuthnContext webAuthnContext;
  }
}

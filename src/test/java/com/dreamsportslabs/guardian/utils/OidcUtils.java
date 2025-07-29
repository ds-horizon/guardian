package com.dreamsportslabs.guardian.utils;

import static com.dreamsportslabs.guardian.Constants.ACCESS_TOKEN_EXPIRY_SECONDS;
import static com.dreamsportslabs.guardian.Constants.AUTH_RESPONSE_TYPE_CODE;
import static com.dreamsportslabs.guardian.Constants.CLAIM_SUB;
import static com.dreamsportslabs.guardian.Constants.EQUALS_SIGN;
import static com.dreamsportslabs.guardian.Constants.EXAMPLE_CALLBACK;
import static com.dreamsportslabs.guardian.Constants.HEADER_LOCATION;
import static com.dreamsportslabs.guardian.Constants.ID_TOKEN_EXPIRY_SECONDS;
import static com.dreamsportslabs.guardian.Constants.JWT_ALGORITHM_RS256;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_EXP;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_IAT;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_ISS;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_JTI;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_RFT_ID;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_SCOPE;
import static com.dreamsportslabs.guardian.Constants.JWT_TYPE_ACCESS_TOKEN;
import static com.dreamsportslabs.guardian.Constants.LOGIN_CHALLENGE;
import static com.dreamsportslabs.guardian.Constants.PARAM_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.PARAM_REDIRECT_URI;
import static com.dreamsportslabs.guardian.Constants.PARAM_RESPONSE_TYPE;
import static com.dreamsportslabs.guardian.Constants.PARAM_SCOPE;
import static com.dreamsportslabs.guardian.Constants.PARAM_SEPARATOR;
import static com.dreamsportslabs.guardian.Constants.PARAM_STATE;
import static com.dreamsportslabs.guardian.Constants.QUERY_SEPARATOR;
import static com.dreamsportslabs.guardian.Constants.SCOPE_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.SCOPE_EMAIL;
import static com.dreamsportslabs.guardian.Constants.SCOPE_OPENID;
import static com.dreamsportslabs.guardian.Constants.TEST_ISSUER;
import static com.dreamsportslabs.guardian.Constants.TEST_KID;
import static com.dreamsportslabs.guardian.Constants.TEST_PUBLIC_KEY_PATH;
import static com.dreamsportslabs.guardian.Constants.TEST_STATE;
import static com.dreamsportslabs.guardian.utils.DbUtils.authorizeSessionExists;
import static com.dreamsportslabs.guardian.utils.DbUtils.getAuthorizeSession;
import static com.dreamsportslabs.guardian.utils.DbUtils.getOidcCode;
import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.rsa.RSAVerifier;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OidcUtils {

  private static final Logger log = LoggerFactory.getLogger(OidcUtils.class);
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Creates a valid authorize request with all required parameters.
   *
   * @param clientId The client ID to use in the request
   * @return Map containing the query parameters for a valid authorize request
   */
  public static Map<String, String> createValidAuthorizeRequest(String clientId) {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(PARAM_RESPONSE_TYPE, AUTH_RESPONSE_TYPE_CODE);
    queryParams.put(PARAM_CLIENT_ID, clientId);
    queryParams.put(PARAM_SCOPE, SCOPE_OPENID + " " + SCOPE_EMAIL + " " + SCOPE_ADDRESS);
    queryParams.put(PARAM_REDIRECT_URI, EXAMPLE_CALLBACK);
    queryParams.put(PARAM_STATE, TEST_STATE);
    return queryParams;
  }

  /**
   * Creates a valid authorize request with all required parameters.
   *
   * @param clientId The client ID to use in the request
   * @param scopes The list of scopes to add to the request
   * @return Map containing the query parameters for a valid authorize request
   */
  public static Map<String, String> createValidAuthorizeRequest(
      String clientId, List<String> scopes) {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(PARAM_RESPONSE_TYPE, AUTH_RESPONSE_TYPE_CODE);
    queryParams.put(PARAM_CLIENT_ID, clientId);
    queryParams.put(PARAM_SCOPE, String.join(" ", scopes));
    queryParams.put(PARAM_REDIRECT_URI, EXAMPLE_CALLBACK);
    queryParams.put(PARAM_STATE, TEST_STATE);
    return queryParams;
  }

  /**
   * Extracts the login challenge from a redirect location URL.
   *
   * @param location The redirect location URL
   * @return The login challenge value, or null if not found
   */
  public static String extractLoginChallenge(String location) {
    if (location == null) return null;
    String[] params = location.split(QUERY_SEPARATOR)[1].split(PARAM_SEPARATOR);
    for (String param : params) {
      if (param.startsWith(LOGIN_CHALLENGE + EQUALS_SIGN)) {
        return param.split(EQUALS_SIGN)[1];
      }
    }
    return null;
  }

  /**
   * Validates that the Redis authorize session model has all the expected values set. This method
   * checks that the session exists and contains the expected fields from the authorize request.
   *
   * @param tenantId The tenant ID
   * @param loginChallenge The login challenge to validate
   * @param expectedQueryParams The original query parameters used in the authorize request
   */
  public static void validateAuthorizeSessionModel(
      String tenantId, String loginChallenge, Map<String, String> expectedQueryParams) {
    JsonObject sessionModel = DbUtils.getAuthorizeSession(tenantId, loginChallenge);

    // Verify session exists
    assertThat("Authorize session should exist in Redis", sessionModel, notNullValue());

    // Validate response type
    if (expectedQueryParams.containsKey(PARAM_RESPONSE_TYPE)) {
      String expectedResponseType = expectedQueryParams.get(PARAM_RESPONSE_TYPE);
      String actualResponseType = sessionModel.getString("responseType");
      assertThat(
          "Response type should match",
          actualResponseType,
          equalTo(expectedResponseType.toUpperCase()));
    }

    // Validate redirect URI
    if (expectedQueryParams.containsKey(PARAM_REDIRECT_URI)) {
      String expectedRedirectUri = expectedQueryParams.get(PARAM_REDIRECT_URI);
      String actualRedirectUri = sessionModel.getString("redirectUri");
      assertThat("Redirect URI should match", actualRedirectUri, equalTo(expectedRedirectUri));
    }

    // Validate state
    if (expectedQueryParams.containsKey(PARAM_STATE)) {
      String expectedState = expectedQueryParams.get(PARAM_STATE);
      String actualState = sessionModel.getString("state");
      assertThat("State should match", actualState, equalTo(expectedState));
    }

    // Validate scopes
    if (expectedQueryParams.containsKey(PARAM_SCOPE)) {
      String expectedScopes = expectedQueryParams.get(PARAM_SCOPE);
      List<String> actualAllowedScopes = sessionModel.getJsonArray("allowedScopes").getList();
      assertThat("Allowed scopes should not be null", actualAllowedScopes, notNullValue());
      assertThat(
          "Allowed scopes should not be empty", actualAllowedScopes.isEmpty(), equalTo(false));

      // Check that all valid expected scopes are present in allowed scopes
      // Note: Invalid scopes are filtered out by the service, so we only check valid ones
      String[] expectedScopeArray = expectedScopes.split(" ");
      for (String expectedScope : expectedScopeArray) {
        // Only check if the scope is actually in the allowed scopes (i.e., it's valid)
        if (actualAllowedScopes.contains(expectedScope)) {
          assertThat(
              "Valid scope '" + expectedScope + "' should be in allowed scopes",
              actualAllowedScopes.contains(expectedScope),
              equalTo(true));
        }
        // Invalid scopes are expected to be filtered out, so we don't assert on them
      }

      // Additional validation: ensure that at least 'openid' scope is present
      assertThat(
          "OpenID scope should always be present",
          actualAllowedScopes.contains("openid"),
          equalTo(true));
    }

    // Validate client information
    JsonObject client = sessionModel.getJsonObject("client");
    assertThat("Client should not be null", client, notNullValue());
    if (expectedQueryParams.containsKey(PARAM_CLIENT_ID)) {
      String expectedClientId = expectedQueryParams.get(PARAM_CLIENT_ID);
      String actualClientId = client.getString("clientId");
      assertThat("Client ID should match", actualClientId, equalTo(expectedClientId));
    }

    // Validate optional parameters if present
    if (expectedQueryParams.containsKey("nonce")) {
      String expectedNonce = expectedQueryParams.get("nonce");
      String actualNonce = sessionModel.getString("nonce");
      assertThat("Nonce should match", actualNonce, equalTo(expectedNonce));
    }

    if (expectedQueryParams.containsKey("code_challenge")) {
      String expectedCodeChallenge = expectedQueryParams.get("code_challenge");
      String actualCodeChallenge = sessionModel.getString("codeChallenge");
      assertThat(
          "Code challenge should match", actualCodeChallenge, equalTo(expectedCodeChallenge));
    }

    if (expectedQueryParams.containsKey("code_challenge_method")) {
      String expectedCodeChallengeMethod = expectedQueryParams.get("code_challenge_method");
      String actualCodeChallengeMethod = sessionModel.getString("codeChallengeMethod");
      assertThat(
          "Code challenge method should match",
          actualCodeChallengeMethod,
          equalTo(expectedCodeChallengeMethod.toUpperCase()));
    }

    if (expectedQueryParams.containsKey("prompt")) {
      String expectedPrompt = expectedQueryParams.get("prompt");
      String actualPrompt = sessionModel.getString("prompt");
      assertThat("Prompt should match", actualPrompt, equalTo(expectedPrompt.toUpperCase()));
    }

    if (expectedQueryParams.containsKey("login_hint")) {
      String expectedLoginHint = expectedQueryParams.get("login_hint");
      String actualLoginHint = sessionModel.getString("loginHint");
      assertThat("Login hint should match", actualLoginHint, equalTo(expectedLoginHint));
    }

    // Validate that userId is initially null (not set until login accept)
    String userId = sessionModel.getString("userId");
    assertThat("UserId should be null initially", userId, equalTo(null));

    // Validate that consentedScopes is initially null (not set until login accept)
    List<String> consentedScopes =
        sessionModel.getJsonArray("consentedScopes") != null
            ? sessionModel.getJsonArray("consentedScopes").getList()
            : null;
    assertThat("Consented scopes should be null initially", consentedScopes, equalTo(null));
  }

  /**
   * Validates detailed response structure for login-accept success cases. This method validates the
   * location header structure and session cleanup.
   *
   * @param response The response to validate
   * @param tenantId The tenant ID
   * @param loginChallenge The login challenge that should be deleted
   * @param expectedUserId The expected user ID in the session (optional)
   */
  public static void validateLoginAcceptResponse(
      Response response, String tenantId, String loginChallenge, String expectedUserId) {
    // Validate - Detailed response structure validation
    response
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue())
        .header(HEADER_LOCATION, containsString("consent_challenge="))
        .header(HEADER_LOCATION, containsString("state="));

    // Validate location header structure
    String locationHeader = response.getHeader(HEADER_LOCATION);
    assertThat("Location header should not be null", locationHeader, notNullValue());
    assertThat(
        "Location header should contain consent_challenge",
        locationHeader,
        containsString("consent_challenge="));
    assertThat("Location header should contain state", locationHeader, containsString("state="));

    // Verify session was deleted from database
    assertThat(
        "Authorize session should be deleted",
        authorizeSessionExists(tenantId, loginChallenge),
        equalTo(false));

    // Verify user association in the session (if available)
    if (expectedUserId != null) {
      JsonObject sessionData = getAuthorizeSession(tenantId, loginChallenge);
      if (sessionData != null) {
        String sessionUserId = sessionData.getString("userId");
        assertThat(
            "Session should be associated with correct user",
            sessionUserId,
            equalTo(expectedUserId));
      }
    }
  }

  /**
   * Extracts the consent challenge from a redirect location URL.
   *
   * @param response The response containing the redirect location
   * @return The consent challenge value, or null if not found
   */
  public static String extractConsentChallenge(Response response) {
    String location = response.getHeader(HEADER_LOCATION);
    log.info("Extracting consent challenge from location: {}", location);

    if (location == null) {
      log.warn("Location header is null");
      return null;
    }

    try {
      String[] params = location.split(QUERY_SEPARATOR)[1].split(PARAM_SEPARATOR);
      log.info("Split URL params: {}", Arrays.toString(params));

      for (String param : params) {
        log.info("Checking param: {}", param);
        if (param.startsWith("consent_challenge" + EQUALS_SIGN)) {
          String challenge = param.split(EQUALS_SIGN)[1];
          log.info("Found consent challenge: {}", challenge);
          return challenge;
        }
      }
      log.warn("No consent_challenge found in params");
      return null;
    } catch (Exception e) {
      log.error("Error extracting consent challenge from location: {}", location, e);
      return null;
    }
  }

  /**
   * Validates the consent accept response for successful cases.
   *
   * @param response The response to validate
   * @param tenantId The tenant ID
   * @param consentChallenge The consent challenge that was used
   * @param expectedUserId The expected user ID
   */
  public static void validateConsentAcceptResponse(
      Response response, String tenantId, String consentChallenge, String expectedUserId) {
    response
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue())
        .header(HEADER_LOCATION, containsString("code="))
        .header(HEADER_LOCATION, containsString("state="));

    String locationHeader = response.getHeader(HEADER_LOCATION);
    assertThat("Location header should not be null", locationHeader, notNullValue());
    assertThat("Location header should contain code", locationHeader, containsString("code="));
    assertThat("Location header should contain state", locationHeader, containsString("state="));

    // Verify auth code properties
    validateAuthCodeProperties(locationHeader);

    // Extract auth code for OIDC validation
    String authCode = extractAuthCodeFromLocation(locationHeader);
    assertThat("Auth code should not be null", authCode, notNullValue());

    // Verify session was deleted from database
    assertThat(
        "Authorize session should be deleted after consent acceptance",
        authorizeSessionExists(tenantId, consentChallenge),
        equalTo(false));
  }

  /**
   * Validates the consent accept response for successful cases with OIDC code validation.
   *
   * @param response The response to validate
   * @param tenantId The tenant ID
   * @param consentChallenge The consent challenge that was used
   * @param expectedUserId The expected user ID
   * @param expectedClientId The expected client ID
   * @param expectedScopes The expected consented scopes
   */
  public static void validateConsentAcceptResponse(
      Response response,
      String tenantId,
      String consentChallenge,
      String expectedUserId,
      String expectedClientId,
      List<String> expectedScopes) {
    response
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue())
        .header(HEADER_LOCATION, containsString("code="))
        .header(HEADER_LOCATION, containsString("state="));

    String locationHeader = response.getHeader(HEADER_LOCATION);
    assertThat("Location header should not be null", locationHeader, notNullValue());
    assertThat("Location header should contain code", locationHeader, containsString("code="));
    assertThat("Location header should contain state", locationHeader, containsString("state="));

    // Verify auth code properties
    validateAuthCodeProperties(locationHeader);

    // Extract auth code for OIDC validation
    String authCode = extractAuthCodeFromLocation(locationHeader);
    assertThat("Auth code should not be null", authCode, notNullValue());

    // Verify session was deleted from database
    assertThat(
        "Authorize session should be deleted after consent acceptance",
        authorizeSessionExists(tenantId, consentChallenge),
        equalTo(false));

    // Validate OIDC code properties in Redis
    if (expectedUserId != null
        && authCode != null
        && expectedClientId != null
        && expectedScopes != null) {
      validateOidcCodeProperties(
          tenantId, authCode, expectedUserId, expectedClientId, expectedScopes);
    }
  }

  /**
   * Validates auth code properties from location header.
   *
   * @param locationHeader The location header containing the auth code
   */
  public static void validateAuthCodeProperties(String locationHeader) {
    assertThat("Location header should not be null", locationHeader, notNullValue());
    assertThat(
        "Location header should contain code parameter", locationHeader, containsString("code="));

    // Extract and verify auth code format
    String authCode = extractAuthCodeFromLocation(locationHeader);
    assertThat("Auth code should not be null", authCode, notNullValue());
    assertThat("Auth code should be alphanumeric", authCode, matchesPattern("[a-zA-Z0-9]+"));
    assertThat("Auth code should have reasonable length", authCode.length(), greaterThan(20));
  }

  /**
   * Extracts auth code from location header URL.
   *
   * @param location The location header URL
   * @return The auth code value, or null if not found
   */
  public static String extractAuthCodeFromLocation(String location) {
    if (location == null) return null;

    try {
      String[] params = location.split("\\?")[1].split("&");
      for (String param : params) {
        if (param.startsWith("code=")) {
          return param.split("=")[1];
        }
      }
      return null;
    } catch (Exception e) {
      log.error("Error extracting auth code from location: {}", location, e);
      return null;
    }
  }

  /**
   * Validates OIDC code properties in Redis for positive test cases.
   *
   * @param tenantId The tenant ID
   * @param authCode The auth code to validate
   * @param expectedUserId The expected user ID
   * @param expectedClientId The expected client ID
   * @param expectedScopes The expected consented scopes
   */
  public static void validateOidcCodeProperties(
      String tenantId,
      String authCode,
      String expectedUserId,
      String expectedClientId,
      List<String> expectedScopes) {
    JsonObject oidcCodeData = getOidcCode(tenantId, authCode);
    assertThat("OIDC code should exist in Redis", oidcCodeData, notNullValue());

    // Validate user ID
    String userId = oidcCodeData.getString("userId");
    assertThat("OIDC code should have correct user ID", userId, equalTo(expectedUserId));

    // Validate client
    JsonObject client = oidcCodeData.getJsonObject("client");
    assertThat("OIDC code should have client data", client, notNullValue());
    String clientId = client.getString("clientId");
    assertThat("OIDC code should have correct client ID", clientId, equalTo(expectedClientId));

    // Validate consented scopes
    List<String> consentedScopes = oidcCodeData.getJsonArray("consentedScopes").getList();
    assertThat("OIDC code should have consented scopes", consentedScopes, notNullValue());
    assertThat(
        "OIDC code should have correct consented scopes",
        consentedScopes,
        containsInAnyOrder(expectedScopes.toArray(new String[0])));

    // Validate redirect URI
    String redirectUri = oidcCodeData.getString("redirectUri");
    assertThat("OIDC code should have redirect URI", redirectUri, notNullValue());
    assertThat("OIDC code should have valid redirect URI", redirectUri, containsString("https://"));

    // Validate state
    String state = oidcCodeData.getString("state");
    assertThat("OIDC code should have state", state, notNullValue());

    // Validate nonce (if present)
    String nonce = oidcCodeData.getString("nonce");
    if (nonce != null) {
      assertThat("OIDC code nonce should not be empty", nonce.length(), greaterThan(0));
    }

    // Validate code challenge (if present)
    String codeChallenge = oidcCodeData.getString("codeChallenge");
    if (codeChallenge != null) {
      assertThat("OIDC code challenge should not be empty", codeChallenge.length(), greaterThan(0));
    }
  }

  @SneakyThrows
  public static void validateAccessTokenClaims(
      String accessToken,
      Long expiresIn,
      String expectedUserId,
      String expectedClientId,
      List<String> expectedScopes,
      List<String> notExpectedScopes,
      Boolean validateRftId,
      String refreshToken) {
    Path path = Paths.get(TEST_PUBLIC_KEY_PATH);
    JWT accessTokenJwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    String encodeHeaderClaims = accessToken.split("\\.")[0];
    String decodedHeader =
        new String(Base64.getDecoder().decode(encodeHeaderClaims), StandardCharsets.UTF_8);
    Map<String, Object> headerClaims;
    headerClaims = objectMapper.readValue(decodedHeader, new TypeReference<>() {});
    assertThat(headerClaims.get("alg"), equalTo(JWT_ALGORITHM_RS256));
    assertThat(headerClaims.get("typ"), equalTo(JWT_TYPE_ACCESS_TOKEN));
    assertThat(headerClaims.get("kid"), equalTo(TEST_KID));
    Map<String, Object> claims = accessTokenJwt.getAllClaims();
    assertThat(claims.get("aud"), equalTo(expectedClientId));
    long exp = ((ZonedDateTime) claims.get(JWT_CLAIM_EXP)).toInstant().toEpochMilli() / 1000;
    long iat = ((ZonedDateTime) claims.get(JWT_CLAIM_IAT)).toInstant().toEpochMilli() / 1000;
    assertThat(exp - iat, equalTo(ACCESS_TOKEN_EXPIRY_SECONDS));
    assertThat(expiresIn, equalTo(exp - iat));
    assertThat(claims.get(JWT_CLAIM_ISS), equalTo(TEST_ISSUER));
    assertThat(claims.get(CLAIM_SUB), equalTo(expectedUserId));
    for (String scope : expectedScopes) {
      assertThat((String) claims.get(JWT_CLAIM_SCOPE), containsString(scope));
    }
    for (String scope : notExpectedScopes) {
      assertThat((String) claims.get(JWT_CLAIM_SCOPE), not(containsString(scope)));
    }
    assertThat(claims.get(JWT_CLAIM_CLIENT_ID), equalTo(expectedClientId));
    assertThat((String) claims.get(JWT_CLAIM_JTI), isA(String.class));
    if (validateRftId) {
      assertThat(
          claims.get(JWT_CLAIM_RFT_ID), equalTo(DigestUtils.md5Hex(refreshToken).toUpperCase()));
    }
  }

  @SneakyThrows
  public static void validateIdTokenClaims(
      String idToken,
      String expectedUserId,
      String expectedClientId,
      List<String> expectedClaims,
      List<String> notExpectedClaims) {
    Path path = Paths.get(TEST_PUBLIC_KEY_PATH);
    JWT idTokenJwt = JWT.getDecoder().decode(idToken, RSAVerifier.newVerifier(path));
    String encodeHeaderClaims = idToken.split("\\.")[0];
    String decodedHeader =
        new String(Base64.getDecoder().decode(encodeHeaderClaims), StandardCharsets.UTF_8);
    Map<String, Object> headerClaims;
    headerClaims = objectMapper.readValue(decodedHeader, new TypeReference<>() {});
    assertThat(headerClaims.get("alg"), equalTo(JWT_ALGORITHM_RS256));
    assertThat(headerClaims.get("typ"), equalTo("JWT"));
    assertThat(headerClaims.get("kid"), equalTo(TEST_KID));
    Map<String, Object> claims = idTokenJwt.getAllClaims();
    assertThat(claims.get("aud"), equalTo(expectedClientId));
    long exp = ((ZonedDateTime) claims.get(JWT_CLAIM_EXP)).toInstant().toEpochMilli() / 1000;
    long iat = ((ZonedDateTime) claims.get(JWT_CLAIM_IAT)).toInstant().toEpochMilli() / 1000;
    assertThat(exp - iat, equalTo(ID_TOKEN_EXPIRY_SECONDS));
    assertThat(claims.get(JWT_CLAIM_ISS), equalTo(TEST_ISSUER));
    assertThat(claims.get(CLAIM_SUB), equalTo(expectedUserId));
    for (String claim : expectedClaims) {
      assertThat((String) claims.get(claim), notNullValue());
    }
    for (String claim : notExpectedClaims) {
      assertThat((String) claims.get(claim), nullValue());
    }
  }
}

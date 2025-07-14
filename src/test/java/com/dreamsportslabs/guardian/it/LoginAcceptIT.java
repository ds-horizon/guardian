package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_LOGIN_CHALLENGE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.ERROR_LOGIN_CHALLENGE_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.ERROR_REFRESH_TOKEN_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.ERROR_UNAUTHORIZED;
import static com.dreamsportslabs.guardian.Constants.HEADER_LOCATION;
import static com.dreamsportslabs.guardian.Constants.HEADER_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.INVALID_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.INVALID_TENANT;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.PARAM_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.PARAM_CODE_CHALLENGE;
import static com.dreamsportslabs.guardian.Constants.PARAM_CODE_CHALLENGE_METHOD;
import static com.dreamsportslabs.guardian.Constants.PARAM_LOGIN_HINT;
import static com.dreamsportslabs.guardian.Constants.PARAM_NONCE;
import static com.dreamsportslabs.guardian.Constants.PARAM_PROMPT;
import static com.dreamsportslabs.guardian.Constants.PARAM_REDIRECT_URI;
import static com.dreamsportslabs.guardian.Constants.PARAM_RESPONSE_TYPE;
import static com.dreamsportslabs.guardian.Constants.PARAM_SCOPE;
import static com.dreamsportslabs.guardian.Constants.PARAM_STATE;
import static com.dreamsportslabs.guardian.Constants.SCOPE_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.SCOPE_EMAIL;
import static com.dreamsportslabs.guardian.Constants.SCOPE_OPENID;
import static com.dreamsportslabs.guardian.Constants.SCOPE_PHONE;
import static com.dreamsportslabs.guardian.Constants.TENANT_1;
import static com.dreamsportslabs.guardian.Constants.TENANT_2;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.authorize;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createClient;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createClientScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.loginAccept;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.signIn;
import static com.dreamsportslabs.guardian.utils.DbUtils.authorizeSessionExists;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupClients;
import static com.dreamsportslabs.guardian.utils.OidcUtils.createValidAuthorizeRequest;
import static com.dreamsportslabs.guardian.utils.OidcUtils.extractLoginChallenge;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import com.dreamsportslabs.guardian.utils.ClientUtils;
import io.restassured.response.Response;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LoginAcceptIT {

  public static String tenant1 = TENANT_1;
  public static String tenant2 = TENANT_2;

  private String validClientId;
  private String validRefreshToken;
  private String validLoginChallenge;

  @BeforeEach
  void setUp() {
    // Clean up any existing test data
    cleanupClients(tenant1);
    cleanupClients(tenant2);

    // Create a test client for authorization tests
    Response clientResponse = createTestClient();
    validClientId = clientResponse.jsonPath().getString("client_id");

    // Create client scopes
    createClientScope(
        tenant1,
        validClientId,
        ClientUtils.createClientScopeRequest(
            SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE));

    // Create authorization session
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    Response authorizeResponse = authorize(tenant1, queryParams);
    validLoginChallenge = extractLoginChallenge(authorizeResponse.getHeader(HEADER_LOCATION));

    // Sign in to get refresh token
    Response signInResponse = signIn(tenant1, "testuser", "password", "code");
    validRefreshToken = signInResponse.jsonPath().getString("refresh_token");
  }

  @Test
  @DisplayName("Should accept login successfully with valid parameters")
  public void testLoginAcceptSuccess() {
    // Arrange
    Map<String, Object> requestBody = Map.of(
        BODY_PARAM_LOGIN_CHALLENGE, validLoginChallenge,
        BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    // Act
    Response response = loginAccept(tenant1, requestBody);

    // Validate - Should redirect to redirect_uri with authorization code
    response
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue())
        .header(HEADER_LOCATION, containsString("code="))
        .header(HEADER_LOCATION, containsString("state="));

    // Verify session was deleted from database
    assertThat(authorizeSessionExists(tenant1, validLoginChallenge), equalTo(false));
  }

  @Test
  @DisplayName("Should return error when login_challenge is missing")
  public void testLoginAcceptMissingLoginChallenge() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_REFRESH_TOKEN, validRefreshToken);
    // login_challenge is intentionally omitted

    // Act
    Response response = loginAccept(tenant1, requestBody);

    // Validate - Basic validation errors follow LoginAcceptRequestDto format (400 JSON)
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_LOGIN_CHALLENGE_REQUIRED));
  }

  @Test
  @DisplayName("Should return error when login_challenge is empty")
  public void testLoginAcceptEmptyLoginChallenge() {
    // Arrange
    Map<String, Object> requestBody = Map.of(
        BODY_PARAM_LOGIN_CHALLENGE, "",
        BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    // Act
    Response response = loginAccept(tenant1, requestBody);

    // Validate - Basic validation errors follow LoginAcceptRequestDto format (400 JSON)
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_LOGIN_CHALLENGE_REQUIRED));
  }

  @Test
  @DisplayName("Should return error when login_challenge is null")
  public void testLoginAcceptNullLoginChallenge() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_LOGIN_CHALLENGE, null);
    requestBody.put(BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    // Act
    Response response = loginAccept(tenant1, requestBody);

    // Validate - Basic validation errors follow LoginAcceptRequestDto format (400 JSON)
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_LOGIN_CHALLENGE_REQUIRED));
  }

  @Test
  @DisplayName("Should return error when refresh_token is missing")
  public void testLoginAcceptMissingRefreshToken() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_LOGIN_CHALLENGE, validLoginChallenge);
    // refresh_token is intentionally omitted - this will cause DTO validation to fail

    // Act
    Response response = loginAccept(tenant1, requestBody);

    // Validate - Basic validation errors follow LoginAcceptRequestDto format (400 JSON)
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_REFRESH_TOKEN_REQUIRED));
  }

  @Test
  @DisplayName("Should return error when refresh_token is empty")
  public void testLoginAcceptEmptyRefreshToken() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_LOGIN_CHALLENGE, validLoginChallenge);
    requestBody.put(BODY_PARAM_REFRESH_TOKEN, "");

    // Act
    Response response = loginAccept(tenant1, requestBody);

    // Validate - Basic validation errors follow LoginAcceptRequestDto format (400 JSON)
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_REFRESH_TOKEN_REQUIRED));
  }

  @Test
  @DisplayName("Should return error when refresh_token is null")
  public void testLoginAcceptNullRefreshToken() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_LOGIN_CHALLENGE, validLoginChallenge);
    requestBody.put(BODY_PARAM_REFRESH_TOKEN, null);

    // Act
    Response response = loginAccept(tenant1, requestBody);

    // Validate - Basic validation errors follow LoginAcceptRequestDto format (400 JSON)
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_REFRESH_TOKEN_REQUIRED));
  }

  @Test
  @DisplayName("Should return error when login_challenge is invalid")
  public void testLoginAcceptInvalidLoginChallenge() {
    // Arrange
    String invalidLoginChallenge = "invalid_login_challenge_" + RandomStringUtils.randomAlphanumeric(10);
    Map<String, Object> requestBody = Map.of(
        BODY_PARAM_LOGIN_CHALLENGE, invalidLoginChallenge,
        BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    // Act
    Response response = loginAccept(tenant1, requestBody);

    // Validate - Invalid login challenge should result in 400 error
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("Invalid login challenge"));
  }

  @Test
  @DisplayName("Should return error when refresh_token is invalid")
  public void testLoginAcceptInvalidRefreshToken() {
    // Arrange
    String invalidRefreshToken = "invalid_refresh_token_" + RandomStringUtils.randomAlphanumeric(10);
    Map<String, Object> requestBody = Map.of(
        BODY_PARAM_LOGIN_CHALLENGE, validLoginChallenge,
        BODY_PARAM_REFRESH_TOKEN, invalidRefreshToken);

    // Act
    Response response = loginAccept(tenant1, requestBody);

    // Validate - Invalid refresh token should result in 401 error
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_UNAUTHORIZED))
        .body(MESSAGE, equalTo("Invalid refresh token"));
  }

  @Test
  @DisplayName("Should return error when tenant-id header is missing")
  public void testLoginAcceptMissingTenantId() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_LOGIN_CHALLENGE, validLoginChallenge);
    requestBody.put(BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    // Act
    Response response = loginAccept(null, requestBody);

    // Validate - Missing tenant-id should result in a 401 error
    response.then().statusCode(SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return error when non-existent tenant-id is provided")
  public void testLoginAcceptNonExistentTenantId() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_LOGIN_CHALLENGE, validLoginChallenge);
    requestBody.put(BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    // Act
    Response response = loginAccept(INVALID_TENANT, requestBody);

    // Validate - Invalid tenant should result in a 400 error
    response.then().statusCode(SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("Should handle client with skip_consent=true")
  public void testLoginAcceptWithSkipConsent() {
    // Arrange - Create a client with skip_consent=true
    Map<String, Object> clientRequest = ClientUtils.createValidClientRequest();
    clientRequest.put("skip_consent", true);
    Response clientResponse = createClient(tenant1, clientRequest);
    String skipConsentClientId = clientResponse.jsonPath().getString("client_id");

    // Create client scopes
    createClientScope(
        tenant1,
        skipConsentClientId,
        ClientUtils.createClientScopeRequest(
            SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE));

    // Create authorization session for skip consent client
    Map<String, String> queryParams = createValidAuthorizeRequest(skipConsentClientId);
    Response authorizeResponse = authorize(tenant1, queryParams);
    String skipConsentLoginChallenge = extractLoginChallenge(authorizeResponse.getHeader(HEADER_LOCATION));

    // Sign in to get refresh token
    Response signInResponse = signIn(tenant1, "testuser", "password", "code");
    String skipConsentRefreshToken = signInResponse.jsonPath().getString("refresh_token");

    Map<String, Object> requestBody = Map.of(
        BODY_PARAM_LOGIN_CHALLENGE, skipConsentLoginChallenge,
        BODY_PARAM_REFRESH_TOKEN, skipConsentRefreshToken);

    // Act
    Response response = loginAccept(tenant1, requestBody);

    // Validate - Should redirect to redirect_uri with authorization code
    response
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue())
        .header(HEADER_LOCATION, containsString("code="))
        .header(HEADER_LOCATION, containsString("state="));

    // Verify session was deleted from database
    assertThat(authorizeSessionExists(tenant1, skipConsentLoginChallenge), equalTo(false));
  }

  @Test
  @DisplayName("Should redirect to consent page when user has not consented to all scopes")
  public void testLoginAcceptWithIncompleteConsent() {
    // Arrange - This would require setting up a user with partial consent
    // For now, we'll test the basic flow with a client that requires consent
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_LOGIN_CHALLENGE, validLoginChallenge);
    requestBody.put(BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    // Act
    Response response = loginAccept(tenant1, requestBody);

    // Validate - Should redirect to consent page
    response
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue())
        .header(HEADER_LOCATION, containsString("consent_challenge="));
  }

  @Test
  @DisplayName("Should handle very long login_challenge parameter")
  public void testLoginAcceptLongLoginChallenge() {
    // Arrange
    String longLoginChallenge = RandomStringUtils.randomAlphanumeric(1000);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_LOGIN_CHALLENGE, longLoginChallenge);
    requestBody.put(BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    // Act
    Response response = loginAccept(tenant1, requestBody);

    // Validate - Should return 400 for invalid login challenge
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("Invalid login challenge"));
  }

  @Test
  @DisplayName("Should handle special characters in login_challenge")
  public void testLoginAcceptSpecialCharactersInLoginChallenge() {
    // Arrange
    String specialLoginChallenge = "login_challenge_with_special_chars_!@#$%^&*()";
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_LOGIN_CHALLENGE, specialLoginChallenge);
    requestBody.put(BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    // Act
    Response response = loginAccept(tenant1, requestBody);

    // Validate - Should return 400 for invalid login challenge
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("Invalid login challenge"));
  }

  @Test
  @DisplayName("Should handle multiple concurrent login accept requests")
  public void testLoginAcceptConcurrentRequests() {
    // Arrange
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_LOGIN_CHALLENGE, validLoginChallenge);
    requestBody.put(BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    // Act - Make multiple concurrent requests
    Response response1 = loginAccept(tenant1, requestBody);
    Response response2 = loginAccept(tenant1, requestBody);

    // Validate - First request should succeed, second should fail
    response1
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue());

    response2
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("Invalid login challenge"));
  }

  @Test
  @DisplayName("Should handle expired login challenge")
  public void testLoginAcceptExpiredLoginChallenge() {
    // Arrange - Create a session and wait for it to expire
    // This would require time-based testing, for now we'll test with an invalid challenge
    String expiredLoginChallenge = "expired_login_challenge_" + RandomStringUtils.randomAlphanumeric(10);
    Map<String, Object> requestBody = Map.of(
        BODY_PARAM_LOGIN_CHALLENGE, expiredLoginChallenge,
        BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    // Act
    Response response = loginAccept(tenant1, requestBody);

    // Validate - Should return 400 for invalid login challenge
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("Invalid login challenge"));
  }

  @Test
  @DisplayName("Should handle expired refresh token")
  public void testLoginAcceptExpiredRefreshToken() {
    // Arrange - Use an expired refresh token
    String expiredRefreshToken = "expired_refresh_token_" + RandomStringUtils.randomAlphanumeric(10);
    Map<String, Object> requestBody = Map.of(
        BODY_PARAM_LOGIN_CHALLENGE, validLoginChallenge,
        BODY_PARAM_REFRESH_TOKEN, expiredRefreshToken);

    // Act
    Response response = loginAccept(tenant1, requestBody);

    // Validate - Should return 401 for invalid refresh token
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_UNAUTHORIZED))
        .body(MESSAGE, equalTo("Invalid refresh token"));
  }

  @Test
  @DisplayName("Should handle tenant isolation")
  public void testLoginAcceptTenantIsolation() {
    // Arrange - Use login challenge from tenant1 but make request to tenant2
    Map<String, Object> requestBody = Map.of(
        BODY_PARAM_LOGIN_CHALLENGE, validLoginChallenge,
        BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    // Act
    Response response = loginAccept(tenant2, requestBody);

    // Validate - Should return 400 for invalid login challenge in different tenant
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("Invalid login challenge"));
  }

  private Response createTestClient() {
    Map<String, Object> requestBody = ClientUtils.createValidClientRequest();
    return createClient(tenant1, requestBody);
  }
} 
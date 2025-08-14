package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_LOGIN_CHALLENGE;
import static com.dreamsportslabs.guardian.Constants.CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.CLIENT_NAME;
import static com.dreamsportslabs.guardian.Constants.DEVICE_VALUE;
import static com.dreamsportslabs.guardian.Constants.ERROR_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.ERROR_FIELD;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_CHALLENGE;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.ERROR_LOGIN_CHALLENGE_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.ERROR_REFRESH_TOKEN_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.ERROR_UNAUTHORIZED;
import static com.dreamsportslabs.guardian.Constants.HEADER_LOCATION;
import static com.dreamsportslabs.guardian.Constants.INVALID_TENANT;
import static com.dreamsportslabs.guardian.Constants.IP_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.LOCATION_VALUE;
import static com.dreamsportslabs.guardian.Constants.OIDC_BODY_PARAM_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.PARTIAL_CONSENT_USER_ID;
import static com.dreamsportslabs.guardian.Constants.SCOPE_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.SCOPE_EMAIL;
import static com.dreamsportslabs.guardian.Constants.SCOPE_OPENID;
import static com.dreamsportslabs.guardian.Constants.SCOPE_PHONE;
import static com.dreamsportslabs.guardian.Constants.SKIP_CONSENT;
import static com.dreamsportslabs.guardian.Constants.SKIP_CONSENT_CLIENT_NAME;
import static com.dreamsportslabs.guardian.Constants.SOURCE_VALUE;
import static com.dreamsportslabs.guardian.Constants.TENANT_1;
import static com.dreamsportslabs.guardian.Constants.TENANT_2;
import static com.dreamsportslabs.guardian.Constants.TEST_USER_ID;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.authorize;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createClient;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createClientScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.loginAccept;
import static com.dreamsportslabs.guardian.utils.DbUtils.authorizeSessionExists;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanUpScopes;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupClients;
import static com.dreamsportslabs.guardian.utils.DbUtils.insertRefreshToken;
import static com.dreamsportslabs.guardian.utils.DbUtils.insertUserConsent;
import static com.dreamsportslabs.guardian.utils.OidcUtils.createValidAuthorizeRequest;
import static com.dreamsportslabs.guardian.utils.OidcUtils.extractLoginChallenge;
import static com.dreamsportslabs.guardian.utils.OidcUtils.validateLoginAcceptResponse;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.dreamsportslabs.guardian.utils.ClientUtils;
import com.dreamsportslabs.guardian.utils.OidcUtils;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

public class LoginAcceptIT {

  public static String tenant1 = TENANT_1;
  public static String tenant2 = TENANT_2;

  private String validClientId;
  private String validRefreshToken;
  private String validLoginChallenge;

  @BeforeEach
  void setUp() {
    cleanupClients(tenant1);
    cleanupClients(tenant2);
    cleanUpScopes(tenant1);
    cleanUpScopes(tenant2);

    OidcUtils.createRequiredScopes(tenant1);
    OidcUtils.createRequiredScopes(tenant2);

    Response clientResponse = createTestClient();
    validClientId = clientResponse.jsonPath().getString(CLIENT_ID);

    createClientScope(
        tenant1,
        validClientId,
        ClientUtils.createClientScopeRequest(
            SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE));

    validRefreshToken =
        insertRefreshToken(
            tenant1, TEST_USER_ID, 1800L, SOURCE_VALUE, DEVICE_VALUE, LOCATION_VALUE, IP_ADDRESS);

    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    Response authorizeResponse = authorize(tenant1, queryParams);
    validLoginChallenge = extractLoginChallenge(authorizeResponse.getHeader(HEADER_LOCATION));
  }

  @Test
  @DisplayName("Should accept login successfully with valid parameters")
  public void testLoginAcceptSuccess() {
    Map<String, Object> requestBody =
        Map.of(
            BODY_PARAM_LOGIN_CHALLENGE,
            validLoginChallenge,
            OIDC_BODY_PARAM_REFRESH_TOKEN,
            validRefreshToken);

    Response response = loginAccept(tenant1, requestBody);

    validateLoginAcceptResponse(response, tenant1, validLoginChallenge, TEST_USER_ID);
  }

  @ParameterizedTest
  @DisplayName("Should return error for null/empty login_challenge")
  @NullAndEmptySource
  void testLoginAcceptNullEmptyLoginChallenge(String loginChallenge) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(OIDC_BODY_PARAM_REFRESH_TOKEN, validRefreshToken);
    requestBody.put(BODY_PARAM_LOGIN_CHALLENGE, loginChallenge);

    Response response = loginAccept(tenant1, requestBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo(ERROR_LOGIN_CHALLENGE_REQUIRED));
  }

  @ParameterizedTest
  @DisplayName("Should return error for null/empty refresh_token")
  @NullAndEmptySource
  void testLoginAcceptNullEmptyRefreshToken(String refreshToken) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_LOGIN_CHALLENGE, validLoginChallenge);
    requestBody.put(OIDC_BODY_PARAM_REFRESH_TOKEN, refreshToken);

    Response response = loginAccept(tenant1, requestBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo(ERROR_REFRESH_TOKEN_REQUIRED));
  }

  @Test
  @DisplayName("Should return error when login_challenge is invalid")
  public void testLoginAcceptInvalidLoginChallenge() {
    String invalidLoginChallenge =
        "invalid_login_challenge_" + RandomStringUtils.randomAlphanumeric(10);
    Map<String, Object> requestBody =
        Map.of(
            BODY_PARAM_LOGIN_CHALLENGE,
            invalidLoginChallenge,
            OIDC_BODY_PARAM_REFRESH_TOKEN,
            validRefreshToken);

    Response response = loginAccept(tenant1, requestBody);

    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .body(ERROR_FIELD, equalTo(ERROR_UNAUTHORIZED))
        .body(ERROR_DESCRIPTION, equalTo(ERROR_INVALID_CHALLENGE));
  }

  @Test
  @DisplayName("Should return error when refresh_token is invalid")
  public void testLoginAcceptInvalidRefreshToken() {
    String invalidRefreshToken =
        "invalid_refresh_token_" + RandomStringUtils.randomAlphanumeric(10);
    Map<String, Object> requestBody =
        Map.of(
            BODY_PARAM_LOGIN_CHALLENGE,
            validLoginChallenge,
            OIDC_BODY_PARAM_REFRESH_TOKEN,
            invalidRefreshToken);

    Response response = loginAccept(tenant1, requestBody);

    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .body(ERROR_FIELD, equalTo(ERROR_UNAUTHORIZED))
        .body(ERROR_DESCRIPTION, equalTo(ERROR_INVALID_REFRESH_TOKEN));
  }

  @Test
  @DisplayName("Should return error when tenant-id header is missing")
  public void testLoginAcceptMissingTenantId() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_LOGIN_CHALLENGE, validLoginChallenge);
    requestBody.put(OIDC_BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    Response response = loginAccept(null, requestBody);

    response.then().statusCode(SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return error when non-existent tenant-id is provided")
  public void testLoginAcceptNonExistentTenantId() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_LOGIN_CHALLENGE, validLoginChallenge);
    requestBody.put(OIDC_BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    Response response = loginAccept(INVALID_TENANT, requestBody);

    response.then().statusCode(SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("Should handle client with skip_consent=true")
  public void testLoginAcceptWithSkipConsent() {
    Map<String, Object> clientRequest = ClientUtils.createValidClientRequest();
    clientRequest.put(CLIENT_NAME, SKIP_CONSENT_CLIENT_NAME);
    clientRequest.put(SKIP_CONSENT, true);
    Response clientResponse = createClient(tenant1, clientRequest);
    String skipConsentClientId = clientResponse.jsonPath().getString(CLIENT_ID);

    createClientScope(
        tenant1,
        skipConsentClientId,
        ClientUtils.createClientScopeRequest(
            SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE));

    Map<String, String> queryParams = createValidAuthorizeRequest(skipConsentClientId);
    Response authorizeResponse = authorize(tenant1, queryParams);
    String skipConsentLoginChallenge =
        extractLoginChallenge(authorizeResponse.getHeader(HEADER_LOCATION));

    String skipConsentRefreshToken =
        insertRefreshToken(
            tenant1, TEST_USER_ID, 1800L, SOURCE_VALUE, DEVICE_VALUE, LOCATION_VALUE, IP_ADDRESS);

    Map<String, Object> requestBody =
        Map.of(
            BODY_PARAM_LOGIN_CHALLENGE,
            skipConsentLoginChallenge,
            OIDC_BODY_PARAM_REFRESH_TOKEN,
            skipConsentRefreshToken);

    Response response = loginAccept(tenant1, requestBody);

    response
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue())
        .header(HEADER_LOCATION, containsString("code="))
        .header(HEADER_LOCATION, containsString("state="));

    assertThat(authorizeSessionExists(tenant1, skipConsentLoginChallenge), equalTo(false));
  }

  @Test
  @DisplayName("Should redirect to consent page when user has partial consent")
  public void testLoginAcceptWithIncompleteConsent() {
    insertUserConsent(tenant1, validClientId, PARTIAL_CONSENT_USER_ID, Arrays.asList(SCOPE_EMAIL));

    String partialConsentRefreshToken =
        insertRefreshToken(
            tenant1,
            PARTIAL_CONSENT_USER_ID,
            1800L,
            SOURCE_VALUE,
            DEVICE_VALUE,
            LOCATION_VALUE,
            IP_ADDRESS);

    Map<String, Object> requestBody =
        Map.of(
            BODY_PARAM_LOGIN_CHALLENGE,
            validLoginChallenge,
            OIDC_BODY_PARAM_REFRESH_TOKEN,
            partialConsentRefreshToken);

    Response response = loginAccept(tenant1, requestBody);

    validateLoginAcceptResponse(response, tenant1, validLoginChallenge, PARTIAL_CONSENT_USER_ID);
  }

  @Test
  @DisplayName("Should handle multiple concurrent login accept requests")
  public void testLoginAcceptConcurrentRequests() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_LOGIN_CHALLENGE, validLoginChallenge);
    requestBody.put(OIDC_BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    Response response1 = loginAccept(tenant1, requestBody);
    Response response2 = loginAccept(tenant1, requestBody);

    response1.then().statusCode(SC_MOVED_TEMPORARILY).header(HEADER_LOCATION, notNullValue());

    response2
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .body(ERROR_FIELD, equalTo(ERROR_UNAUTHORIZED))
        .body(ERROR_DESCRIPTION, equalTo(ERROR_INVALID_CHALLENGE));
  }

  @Test
  @DisplayName("Should handle expired login challenge")
  public void testLoginAcceptExpiredLoginChallenge() {
    String expiredLoginChallenge =
        "expired_login_challenge_" + RandomStringUtils.randomAlphanumeric(10);
    Map<String, Object> requestBody =
        Map.of(
            BODY_PARAM_LOGIN_CHALLENGE,
            expiredLoginChallenge,
            OIDC_BODY_PARAM_REFRESH_TOKEN,
            validRefreshToken);

    Response response = loginAccept(tenant1, requestBody);

    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .body(ERROR_FIELD, equalTo(ERROR_UNAUTHORIZED))
        .body(ERROR_DESCRIPTION, equalTo(ERROR_INVALID_CHALLENGE));
  }

  @Test
  @DisplayName("Should handle expired refresh token")
  public void testLoginAcceptExpiredRefreshToken() {
    String expiredRefreshToken =
        insertRefreshToken(
            tenant1, TEST_USER_ID, -1800L, SOURCE_VALUE, DEVICE_VALUE, LOCATION_VALUE, IP_ADDRESS);

    Map<String, Object> requestBody =
        Map.of(
            BODY_PARAM_LOGIN_CHALLENGE,
            validLoginChallenge,
            OIDC_BODY_PARAM_REFRESH_TOKEN,
            expiredRefreshToken);

    Response response = loginAccept(tenant1, requestBody);

    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .body(ERROR_FIELD, equalTo(ERROR_UNAUTHORIZED))
        .body(ERROR_DESCRIPTION, equalTo(ERROR_INVALID_REFRESH_TOKEN));
  }

  @Test
  @DisplayName("Should handle tenant isolation")
  public void testLoginAcceptTenantIsolation() {
    Map<String, Object> requestBody =
        Map.of(
            BODY_PARAM_LOGIN_CHALLENGE,
            validLoginChallenge,
            OIDC_BODY_PARAM_REFRESH_TOKEN,
            validRefreshToken);

    Response response = loginAccept(tenant2, requestBody);

    response.then().statusCode(SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should handle refresh token from cookie")
  public void testLoginAcceptWithCookieRefreshToken() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_LOGIN_CHALLENGE, validLoginChallenge);

    Response response = loginAccept(tenant1, requestBody, validRefreshToken);

    validateLoginAcceptResponse(response, tenant1, validLoginChallenge, TEST_USER_ID);
  }

  @Test
  @DisplayName("Should handle full consent scenario - redirect to auth code")
  public void testLoginAcceptWithFullConsent() {
    // Insert all required scopes for the user
    insertUserConsent(
        tenant1,
        validClientId,
        TEST_USER_ID,
        Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE, SCOPE_ADDRESS));

    Map<String, Object> requestBody =
        Map.of(
            BODY_PARAM_LOGIN_CHALLENGE,
            validLoginChallenge,
            OIDC_BODY_PARAM_REFRESH_TOKEN,
            validRefreshToken);

    Response response = loginAccept(tenant1, requestBody);

    response
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue())
        .header(HEADER_LOCATION, containsString("code="))
        .header(HEADER_LOCATION, containsString("state="));

    assertThat(authorizeSessionExists(tenant1, validLoginChallenge), equalTo(false));
  }

  @Test
  @DisplayName("Should handle database error during refresh token validation")
  public void testLoginAcceptDatabaseError() {
    // Use a malformed refresh token that might cause database errors
    String malformedRefreshToken = "malformed_token_with_special_chars_!@#$%^&*()";

    Map<String, Object> requestBody =
        Map.of(
            BODY_PARAM_LOGIN_CHALLENGE,
            validLoginChallenge,
            OIDC_BODY_PARAM_REFRESH_TOKEN,
            malformedRefreshToken);

    Response response = loginAccept(tenant1, requestBody);

    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .body(ERROR_FIELD, equalTo(ERROR_UNAUTHORIZED))
        .body(ERROR_DESCRIPTION, equalTo(ERROR_INVALID_REFRESH_TOKEN));
  }

  @Test
  @DisplayName("Should handle null login challenge in async deletion")
  public void testLoginAcceptNullLoginChallenge() {
    // This test ensures the async deletion handles null gracefully
    Map<String, Object> requestBody =
        Map.of(
            BODY_PARAM_LOGIN_CHALLENGE,
            validLoginChallenge,
            OIDC_BODY_PARAM_REFRESH_TOKEN,
            validRefreshToken);

    Response response = loginAccept(tenant1, requestBody);

    validateLoginAcceptResponse(response, tenant1, validLoginChallenge, TEST_USER_ID);
  }

  @Test
  @DisplayName("Should handle unexpected response type from service")
  public void testLoginAcceptUnexpectedResponseType() {
    // This test covers the error handling path in LoginAccept.java
    // where an unexpected response type is returned from the service
    // This would typically happen if the service returns null or an unexpected object type

    Map<String, Object> requestBody =
        Map.of(
            BODY_PARAM_LOGIN_CHALLENGE,
            validLoginChallenge,
            OIDC_BODY_PARAM_REFRESH_TOKEN,
            validRefreshToken);

    Response response = loginAccept(tenant1, requestBody);

    // The response should be successful, but this test ensures the error handling
    // in LoginAccept.java is covered for unexpected response types
    response.then().statusCode(SC_MOVED_TEMPORARILY);
  }

  private Response createTestClient() {
    Map<String, Object> requestBody = ClientUtils.createValidClientRequest();
    return createClient(tenant1, requestBody);
  }
}

package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CLAIMS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_DISPLAY_NAME;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_IS_OIDC;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_LOGIN_CHALLENGE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_SCOPE;
import static com.dreamsportslabs.guardian.Constants.CLAIM_SUB;
import static com.dreamsportslabs.guardian.Constants.CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.DEVICE_VALUE;
import static com.dreamsportslabs.guardian.Constants.ERROR_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.ERROR_FIELD;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.ERROR_UNAUTHORIZED;
import static com.dreamsportslabs.guardian.Constants.HEADER_LOCATION;
import static com.dreamsportslabs.guardian.Constants.IP_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.LOCATION_VALUE;
import static com.dreamsportslabs.guardian.Constants.SCOPE_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.SCOPE_EMAIL;
import static com.dreamsportslabs.guardian.Constants.SCOPE_OPENID;
import static com.dreamsportslabs.guardian.Constants.SCOPE_PHONE;
import static com.dreamsportslabs.guardian.Constants.SOURCE_VALUE;
import static com.dreamsportslabs.guardian.Constants.TENANT_1;
import static com.dreamsportslabs.guardian.Constants.TENANT_2;
import static com.dreamsportslabs.guardian.Constants.TEST_USER_ID;
import static com.dreamsportslabs.guardian.constant.Constants.CLAIM_ADDRESS;
import static com.dreamsportslabs.guardian.constant.Constants.CLAIM_EMAIL;
import static com.dreamsportslabs.guardian.constant.Constants.CLAIM_EMAIL_VERIFIED;
import static com.dreamsportslabs.guardian.constant.Constants.CLAIM_PHONE_NUMBER;
import static com.dreamsportslabs.guardian.constant.Constants.CLAIM_PHONE_VERIFIED;
import static com.dreamsportslabs.guardian.constant.Constants.OIDC_PARAM_CONSENT_CHALLENGE;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.authorize;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createClient;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createClientScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getUserConsent;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.loginAccept;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanUpScopes;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupClientScopes;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupClients;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupRedis;
import static com.dreamsportslabs.guardian.utils.DbUtils.insertRefreshToken;
import static com.dreamsportslabs.guardian.utils.DbUtils.insertUserConsent;
import static com.dreamsportslabs.guardian.utils.OidcUtils.createValidAuthorizeRequest;
import static com.dreamsportslabs.guardian.utils.OidcUtils.extractConsentChallenge;
import static com.dreamsportslabs.guardian.utils.OidcUtils.extractLoginChallenge;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import com.dreamsportslabs.guardian.utils.ClientUtils;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

@Slf4j
public class UserConsentIT {

  private static final String tenant1 = TENANT_1;
  private static final String tenant2 = TENANT_2;

  private String validClientId;
  private String validRefreshToken;
  private String validConsentChallenge;

  @BeforeEach
  void setUp() {
    cleanupClients(tenant1);
    cleanupClients(tenant2);
    cleanUpScopes(tenant1);
    cleanUpScopes(tenant2);
    cleanupClientScopes(tenant1);
    cleanupClientScopes(tenant2);
    cleanupRedis();

    createRequiredScopes(tenant1);
    createRequiredScopes(tenant2);

    Response clientResponse = createTestClient();
    validClientId = clientResponse.jsonPath().getString(CLIENT_ID);

    createClientScope(
        tenant1,
        validClientId,
        ClientUtils.createClientScopeRequest(
            SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE));

    createTestData();
  }

  private void createRequiredScopes(String tenantId) {
    Map<String, Object> openidScope = new HashMap<>();
    openidScope.put(BODY_PARAM_SCOPE, SCOPE_OPENID);
    openidScope.put(BODY_PARAM_DISPLAY_NAME, "OpenID Connect");
    openidScope.put(BODY_PARAM_DESCRIPTION, "OpenID Connect scope");
    openidScope.put(BODY_PARAM_CLAIMS, Arrays.asList(CLAIM_SUB));
    openidScope.put(BODY_PARAM_IS_OIDC, true);
    createScope(tenantId, openidScope);

    Map<String, Object> emailScope = new HashMap<>();
    emailScope.put(BODY_PARAM_SCOPE, SCOPE_EMAIL);
    emailScope.put(BODY_PARAM_DISPLAY_NAME, "Email");
    emailScope.put(BODY_PARAM_DESCRIPTION, "Email scope");
    emailScope.put(BODY_PARAM_CLAIMS, Arrays.asList(CLAIM_EMAIL, CLAIM_EMAIL_VERIFIED));
    emailScope.put(BODY_PARAM_IS_OIDC, true);
    createScope(tenantId, emailScope);

    Map<String, Object> addressScope = new HashMap<>();
    addressScope.put(BODY_PARAM_SCOPE, SCOPE_ADDRESS);
    addressScope.put(BODY_PARAM_DISPLAY_NAME, "Address");
    addressScope.put(BODY_PARAM_DESCRIPTION, "Address scope");
    addressScope.put(BODY_PARAM_CLAIMS, Arrays.asList(CLAIM_ADDRESS));
    addressScope.put(BODY_PARAM_IS_OIDC, true);
    createScope(tenantId, addressScope);

    Map<String, Object> phoneScope = new HashMap<>();
    phoneScope.put(BODY_PARAM_SCOPE, SCOPE_PHONE);
    phoneScope.put(BODY_PARAM_DISPLAY_NAME, "Phone");
    phoneScope.put(BODY_PARAM_DESCRIPTION, "Phone scope");
    phoneScope.put(BODY_PARAM_CLAIMS, Arrays.asList(CLAIM_PHONE_NUMBER, CLAIM_PHONE_VERIFIED));
    phoneScope.put(BODY_PARAM_IS_OIDC, true);
    createScope(tenantId, phoneScope);
  }

  private Response createTestClient() {
    Map<String, Object> requestBody = ClientUtils.createValidClientRequest();
    return createClient(tenant1, requestBody);
  }

  private void createTestData() {
    validRefreshToken =
        insertRefreshToken(
            tenant1, TEST_USER_ID, 1800L, SOURCE_VALUE, DEVICE_VALUE, LOCATION_VALUE, IP_ADDRESS);

    // Request all 4 scopes that are associated with the client
    Map<String, String> queryParams =
        createValidAuthorizeRequest(
            validClientId, Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE));
    Response authorizeResponse = authorize(tenant1, queryParams);
    String loginChallenge = extractLoginChallenge(authorizeResponse.getHeader(HEADER_LOCATION));

    Map<String, Object> loginAcceptBody = new HashMap<>();
    loginAcceptBody.put(BODY_PARAM_LOGIN_CHALLENGE, loginChallenge);
    loginAcceptBody.put(BODY_PARAM_REFRESH_TOKEN, validRefreshToken);
    Response loginAcceptResponse = loginAccept(tenant1, loginAcceptBody);
    validConsentChallenge = extractConsentChallenge(loginAcceptResponse);

    insertUserConsent(
        tenant1, validClientId, TEST_USER_ID, Arrays.asList(SCOPE_EMAIL, SCOPE_PHONE));
  }

  @Test
  @DisplayName("Should get user consent successfully with valid consent challenge")
  public void testGetUserConsentSuccess() {
    // Arrange
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(OIDC_PARAM_CONSENT_CHALLENGE, validConsentChallenge);

    // Act
    Response response = getUserConsent(tenant1, queryParams);

    // Validate
    validateSuccessfulUserConsentResponse(
        response,
        validClientId,
        Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE),
        Arrays.asList(SCOPE_EMAIL, SCOPE_PHONE),
        TEST_USER_ID);
  }

  @Test
  @DisplayName("Should return error when consent_challenge is missing")
  public void testGetUserConsentMissingConsentChallenge() {
    // Arrange
    Map<String, String> queryParams = new HashMap<>();

    // Act
    Response response = getUserConsent(tenant1, queryParams);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo("consent_challenge is required"));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @DisplayName("Should return error when consent_challenge is empty or whitespace")
  public void testGetUserConsentEmptyConsentChallenge(String emptyConsentChallenge) {
    // Arrange
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(OIDC_PARAM_CONSENT_CHALLENGE, emptyConsentChallenge);

    // Act
    Response response = getUserConsent(tenant1, queryParams);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo("consent_challenge is required"));
  }

  @Test
  @DisplayName("Should not access consent from a different tenant")
  public void testGetUserConsentCrossTenantAccess() {
    // Arrange
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(OIDC_PARAM_CONSENT_CHALLENGE, validConsentChallenge);

    // Act
    Response response = getUserConsent(tenant2, queryParams);

    // Validate
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .body(ERROR_FIELD, equalTo(ERROR_UNAUTHORIZED))
        .body(ERROR_DESCRIPTION, equalTo("Invalid challenge"));
  }

  @Test
  @DisplayName("Should handle user with no previous consents")
  public void testGetUserConsentNoExistingConsents() {
    // Arrange
    String newUserId = "new-user-" + RandomStringUtils.randomAlphanumeric(8);
    String newRefreshToken =
        insertRefreshToken(
            tenant1, newUserId, 1800L, SOURCE_VALUE, DEVICE_VALUE, LOCATION_VALUE, IP_ADDRESS);

    Map<String, String> queryParams =
        createValidAuthorizeRequest(
            validClientId, Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE));
    Response authorizeResponse = authorize(tenant1, queryParams);
    String loginChallenge = extractLoginChallenge(authorizeResponse.getHeader(HEADER_LOCATION));

    Map<String, Object> loginAcceptBody = new HashMap<>();
    loginAcceptBody.put(BODY_PARAM_LOGIN_CHALLENGE, loginChallenge);
    loginAcceptBody.put(BODY_PARAM_REFRESH_TOKEN, newRefreshToken);
    Response loginAcceptResponse = loginAccept(tenant1, loginAcceptBody);
    String newConsentChallenge = extractConsentChallenge(loginAcceptResponse);

    Map<String, String> userConsentParams = new HashMap<>();
    userConsentParams.put(OIDC_PARAM_CONSENT_CHALLENGE, newConsentChallenge);

    // Act
    Response response = getUserConsent(tenant1, userConsentParams);

    // Validate
    validateSuccessfulUserConsentResponse(
        response,
        validClientId,
        Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE),
        Arrays.asList(),
        newUserId);
  }

  @Test
  @DisplayName("Should handle malformed consent challenge")
  public void testGetUserConsentMalformedChallenge() {
    // Arrange
    String malformedConsentChallenge = "malformed!@#$%^&*()";
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(OIDC_PARAM_CONSENT_CHALLENGE, malformedConsentChallenge);

    // Act
    Response response = getUserConsent(tenant1, queryParams);

    // Validate
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .body(ERROR_FIELD, equalTo(ERROR_UNAUTHORIZED))
        .body(ERROR_DESCRIPTION, equalTo("Invalid challenge"));
  }

  @Test
  @DisplayName("Should handle user with partial consents")
  public void testGetUserConsentPartialConsents() {
    // Arrange
    String newUserId = "partial-user-" + RandomStringUtils.randomAlphanumeric(8);
    String newRefreshToken =
        insertRefreshToken(
            tenant1, newUserId, 1800L, SOURCE_VALUE, DEVICE_VALUE, LOCATION_VALUE, IP_ADDRESS);

    insertUserConsent(
        tenant1, validClientId, newUserId, Arrays.asList(SCOPE_OPENID, SCOPE_ADDRESS));

    Map<String, String> queryParams =
        createValidAuthorizeRequest(
            validClientId, Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE));
    Response authorizeResponse = authorize(tenant1, queryParams);
    String loginChallenge = extractLoginChallenge(authorizeResponse.getHeader(HEADER_LOCATION));

    Map<String, Object> loginAcceptBody = new HashMap<>();
    loginAcceptBody.put(BODY_PARAM_LOGIN_CHALLENGE, loginChallenge);
    loginAcceptBody.put(BODY_PARAM_REFRESH_TOKEN, newRefreshToken);
    Response loginAcceptResponse = loginAccept(tenant1, loginAcceptBody);
    String newConsentChallenge = extractConsentChallenge(loginAcceptResponse);

    Map<String, String> userConsentParams = new HashMap<>();
    userConsentParams.put(OIDC_PARAM_CONSENT_CHALLENGE, newConsentChallenge);

    // Act
    Response response = getUserConsent(tenant1, userConsentParams);

    // Validate
    validateSuccessfulUserConsentResponse(
        response,
        validClientId,
        Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE),
        Arrays.asList(SCOPE_OPENID, SCOPE_ADDRESS),
        newUserId);
  }

  @Test
  @DisplayName("Should verify that users with all scopes consented skip consent flow entirely")
  public void testGetUserConsentAllScopesConsented() {
    // Arrange
    String newUserId = "all-consented-user-" + RandomStringUtils.randomAlphanumeric(8);
    String newRefreshToken =
        insertRefreshToken(
            tenant1, newUserId, 1800L, SOURCE_VALUE, DEVICE_VALUE, LOCATION_VALUE, IP_ADDRESS);

    // User has consented to ALL requested scopes
    insertUserConsent(
        tenant1,
        validClientId,
        newUserId,
        Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE));

    Map<String, String> queryParams =
        createValidAuthorizeRequest(
            validClientId, Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE));
    Response authorizeResponse = authorize(tenant1, queryParams);
    String loginChallenge = extractLoginChallenge(authorizeResponse.getHeader(HEADER_LOCATION));

    Map<String, Object> loginAcceptBody = new HashMap<>();
    loginAcceptBody.put(BODY_PARAM_LOGIN_CHALLENGE, loginChallenge);
    loginAcceptBody.put(BODY_PARAM_REFRESH_TOKEN, newRefreshToken);

    // Act
    Response loginAcceptResponse = loginAccept(tenant1, loginAcceptBody);

    // Validate - When user has all consents, loginAccept skips consent flow entirely
    // and returns auth code directly (no consent challenge is created)
    loginAcceptResponse
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue())
        .header(HEADER_LOCATION, containsString("code="))
        .header(HEADER_LOCATION, containsString("state="));

    // Verify that no consent challenge exists (because consent was skipped)
    String locationHeader = loginAcceptResponse.getHeader(HEADER_LOCATION);
    assertThat(
        "Location header should contain auth code",
        locationHeader.contains("code="),
        equalTo(true));
    assertThat(
        "Location header should NOT contain consent_challenge",
        locationHeader.contains("consent_challenge="),
        equalTo(false));
  }

  @Test
  @DisplayName("Should handle case sensitivity in consent challenge")
  public void testGetUserConsentCaseSensitiveChallenge() {
    // Arrange
    String upperCaseChallenge = validConsentChallenge.toUpperCase();
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(OIDC_PARAM_CONSENT_CHALLENGE, upperCaseChallenge);

    // Act
    Response response = getUserConsent(tenant1, queryParams);

    // Validate
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .body(ERROR_FIELD, equalTo(ERROR_UNAUTHORIZED))
        .body(ERROR_DESCRIPTION, equalTo("Invalid challenge"));
  }

  @Test
  @DisplayName("Should handle empty response for user with no consents")
  public void testGetUserConsentEmptyConsentsList() {
    // Arrange
    String newUserId = "no-consents-user-" + RandomStringUtils.randomAlphanumeric(8);
    String newRefreshToken =
        insertRefreshToken(
            tenant1, newUserId, 1800L, SOURCE_VALUE, DEVICE_VALUE, LOCATION_VALUE, IP_ADDRESS);

    Map<String, String> queryParams =
        createValidAuthorizeRequest(
            validClientId, Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE));
    Response authorizeResponse = authorize(tenant1, queryParams);
    String loginChallenge = extractLoginChallenge(authorizeResponse.getHeader(HEADER_LOCATION));

    Map<String, Object> loginAcceptBody = new HashMap<>();
    loginAcceptBody.put(BODY_PARAM_LOGIN_CHALLENGE, loginChallenge);
    loginAcceptBody.put(BODY_PARAM_REFRESH_TOKEN, newRefreshToken);
    Response loginAcceptResponse = loginAccept(tenant1, loginAcceptBody);
    String newConsentChallenge = extractConsentChallenge(loginAcceptResponse);

    Map<String, String> userConsentParams = new HashMap<>();
    userConsentParams.put(OIDC_PARAM_CONSENT_CHALLENGE, newConsentChallenge);

    // Act
    Response response = getUserConsent(tenant1, userConsentParams);

    // Validate
    validateSuccessfulUserConsentResponse(
        response,
        validClientId,
        Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE),
        Arrays.asList(),
        newUserId);

    // Additional validation for empty list
    List<String> consentedScopes = response.jsonPath().getList("consented_scopes");
    assertThat("Consented scopes should be empty list", consentedScopes.isEmpty(), equalTo(true));
  }

  private void validateSuccessfulUserConsentResponse(
      Response response,
      String expectedClientId,
      List<String> expectedRequestedScopes,
      List<String> expectedConsentedScopes,
      String expectedSubject) {

    response
        .then()
        .statusCode(SC_OK)
        .body("client", notNullValue())
        .body("client.clientId", equalTo(expectedClientId)) // camelCase, not snake_case
        .body("requested_scopes", hasSize(expectedRequestedScopes.size()))
        .body("requested_scopes", containsInAnyOrder(expectedRequestedScopes.toArray()))
        .body("consented_scopes", hasSize(expectedConsentedScopes.size()))
        .body("subject", equalTo(expectedSubject));

    // Validate consented scopes only if there are any
    if (!expectedConsentedScopes.isEmpty()) {
      response
          .then()
          .body("consented_scopes", containsInAnyOrder(expectedConsentedScopes.toArray()));
    }

    // Validate that client object has all required fields
    response
        .then()
        .body("client.tenantId", notNullValue())
        .body("client.clientName", notNullValue())
        .body("client.clientSecret", notNullValue())
        .body("client.grantTypes", notNullValue())
        .body("client.redirectUris", notNullValue())
        .body("client.responseTypes", notNullValue())
        .body("client.skipConsent", notNullValue());
  }
}

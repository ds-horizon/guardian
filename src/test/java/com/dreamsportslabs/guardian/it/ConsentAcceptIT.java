package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CLAIMS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CONSENTED_SCOPES;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CONSENT_CHALLENGE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_DISPLAY_NAME;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_IS_OIDC;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_LOGIN_CHALLENGE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_SCOPE;
import static com.dreamsportslabs.guardian.Constants.CLAIM_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.CLAIM_EMAIL;
import static com.dreamsportslabs.guardian.Constants.CLAIM_EMAIL_VERIFIED;
import static com.dreamsportslabs.guardian.Constants.CLAIM_PHONE_NUMBER;
import static com.dreamsportslabs.guardian.Constants.CLAIM_PHONE_NUMBER_VERIFIED;
import static com.dreamsportslabs.guardian.Constants.CLAIM_SUB;
import static com.dreamsportslabs.guardian.Constants.CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.DEVICE_VALUE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.ERROR_FIELD;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.ERROR_UNAUTHORIZED;
import static com.dreamsportslabs.guardian.Constants.HEADER_LOCATION;
import static com.dreamsportslabs.guardian.Constants.IP_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.LOCATION_VALUE;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.SCOPE_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.SCOPE_EMAIL;
import static com.dreamsportslabs.guardian.Constants.SCOPE_OPENID;
import static com.dreamsportslabs.guardian.Constants.SCOPE_PHONE;
import static com.dreamsportslabs.guardian.Constants.SOURCE_VALUE;
import static com.dreamsportslabs.guardian.Constants.TENANT_1;
import static com.dreamsportslabs.guardian.Constants.TENANT_2;
import static com.dreamsportslabs.guardian.Constants.TEST_USER_ID;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.authorize;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.consentAccept;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createClient;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createClientScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.loginAccept;
import static com.dreamsportslabs.guardian.utils.DbUtils.authorizeSessionExists;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanUpScopes;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupClients;
import static com.dreamsportslabs.guardian.utils.DbUtils.expireAuthorizeSession;
import static com.dreamsportslabs.guardian.utils.DbUtils.insertRefreshToken;
import static com.dreamsportslabs.guardian.utils.DbUtils.insertUserConsent;
import static com.dreamsportslabs.guardian.utils.OidcUtils.createValidAuthorizeRequest;
import static com.dreamsportslabs.guardian.utils.OidcUtils.extractAuthCodeFromLocation;
import static com.dreamsportslabs.guardian.utils.OidcUtils.extractConsentChallenge;
import static com.dreamsportslabs.guardian.utils.OidcUtils.extractLoginChallenge;
import static com.dreamsportslabs.guardian.utils.OidcUtils.validateConsentAcceptResponse;
import static com.dreamsportslabs.guardian.utils.OidcUtils.validateOidcCodeProperties;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.dreamsportslabs.guardian.utils.ClientUtils;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@Slf4j
public class ConsentAcceptIT {

  public static String tenant1 = TENANT_1;
  public static String tenant2 = TENANT_2;

  private String validClientId;
  private String validRefreshToken;
  private String validConsentChallenge;

  @BeforeEach
  void setUp() {
    cleanupClients(tenant1);
    cleanupClients(tenant2);
    cleanUpScopes(tenant1);
    cleanUpScopes(tenant2);

    createRequiredScopes(tenant1);
    createRequiredScopes(tenant2);

    Response clientResponse = createTestClient();
    validClientId = clientResponse.jsonPath().getString(CLIENT_ID);

    createClientScope(
        tenant1,
        validClientId,
        ClientUtils.createClientScopeRequest(
            Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE, SCOPE_ADDRESS)));

    createTestData();
  }

  private Response createTestClient() {
    Map<String, Object> clientRequest = new HashMap<>();
    clientRequest.put("client_name", "Test Consent Client");
    clientRequest.put("client_uri", "https://example.com");
    clientRequest.put("redirect_uris", Arrays.asList("https://example.com/callback"));
    clientRequest.put("grant_types", Arrays.asList("authorization_code", "refresh_token"));
    clientRequest.put("response_types", Arrays.asList("code"));
    clientRequest.put("skip_consent", false);

    return createClient(tenant1, clientRequest);
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
    phoneScope.put(
        BODY_PARAM_CLAIMS, Arrays.asList(CLAIM_PHONE_NUMBER, CLAIM_PHONE_NUMBER_VERIFIED));
    phoneScope.put(BODY_PARAM_IS_OIDC, true);
    createScope(tenantId, phoneScope);
  }

  private void createTestData() {
    validRefreshToken =
        insertRefreshToken(
            tenant1, TEST_USER_ID, 3600L, SOURCE_VALUE, DEVICE_VALUE, LOCATION_VALUE, IP_ADDRESS);

    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);

    Response authorizeResponse = authorize(tenant1, queryParams);

    String loginChallenge = extractLoginChallenge(authorizeResponse.getHeader(HEADER_LOCATION));

    Map<String, Object> loginAcceptBody = new HashMap<>();
    loginAcceptBody.put(BODY_PARAM_LOGIN_CHALLENGE, loginChallenge);
    loginAcceptBody.put(BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    Response loginAcceptResponse = loginAccept(tenant1, loginAcceptBody);

    validConsentChallenge = extractConsentChallenge(loginAcceptResponse);
  }

  private Map<String, Object> createRequestBody(
      String consentChallenge, List<String> consentedScopes, String refreshToken) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CONSENT_CHALLENGE, consentChallenge);
    requestBody.put(BODY_PARAM_CONSENTED_SCOPES, consentedScopes);
    requestBody.put(BODY_PARAM_REFRESH_TOKEN, refreshToken);
    return requestBody;
  }

  @Test
  @DisplayName("Should accept consent successfully with valid parameters")
  public void testConsentAcceptSuccess() {
    Map<String, Object> requestBody =
        createRequestBody(
            validConsentChallenge,
            Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE),
            validRefreshToken);

    Response response = consentAccept(tenant1, requestBody);

    validateConsentAcceptResponse(
        response,
        tenant1,
        validConsentChallenge,
        TEST_USER_ID,
        validClientId,
        Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @DisplayName("Should handle null/empty consent_challenge")
  void testConsentAcceptNullEmptyConsentChallenge(String consentChallenge) {
    Map<String, Object> requestBody =
        createRequestBody(consentChallenge, Arrays.asList(SCOPE_EMAIL), validRefreshToken);

    Response response = consentAccept(tenant1, requestBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo("consentChallenge is required"));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @DisplayName("Should handle null/empty refresh_token")
  void testConsentAcceptNullEmptyRefreshToken(String refreshToken) {
    Map<String, Object> requestBody =
        createRequestBody(validConsentChallenge, Arrays.asList(SCOPE_EMAIL), refreshToken);

    Response response = consentAccept(tenant1, requestBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo("refreshToken is required"));
  }

  @Test
  @DisplayName("Should return error when consent_challenge is invalid")
  public void testConsentAcceptInvalidConsentChallenge() {
    Map<String, Object> requestBody =
        createRequestBody("invalid_challenge", Arrays.asList(SCOPE_EMAIL), validRefreshToken);

    Response response = consentAccept(tenant1, requestBody);

    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .body(ERROR_FIELD, equalTo(ERROR_UNAUTHORIZED))
        .body(ERROR_DESCRIPTION, equalTo("Invalid challenge"));
  }

  @Test
  @DisplayName("Should return error when refresh_token is invalid")
  public void testConsentAcceptInvalidRefreshToken() {
    Map<String, Object> requestBody =
        createRequestBody(validConsentChallenge, Arrays.asList(SCOPE_EMAIL), "invalid_token");

    Response response = consentAccept(tenant1, requestBody);

    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .body(ERROR_FIELD, equalTo(ERROR_UNAUTHORIZED))
        .body(ERROR_DESCRIPTION, equalTo("Invalid refresh token"));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @DisplayName("Should handle null/empty consented_scopes")
  void testConsentAcceptNullEmptyConsentedScopes(List<String> consentedScopes) {
    Map<String, Object> requestBody =
        createRequestBody(validConsentChallenge, consentedScopes, validRefreshToken);

    Response response = consentAccept(tenant1, requestBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo("Atleast openid scope has to be consented"));
  }

  @Test
  @DisplayName("Should handle refresh token from cookie")
  public void testConsentAcceptWithCookieRefreshToken() {
    Map<String, Object> requestBody =
        createRequestBody(validConsentChallenge, Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL), null);

    Response response = consentAccept(tenant1, requestBody, validRefreshToken);

    validateConsentAcceptResponse(
        response,
        tenant1,
        validConsentChallenge,
        TEST_USER_ID,
        validClientId,
        Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL));
  }

  @Test
  @DisplayName("Should handle existing consented scopes")
  public void testConsentAcceptWithExistingConsents() {
    insertUserConsent(
        tenant1, validClientId, TEST_USER_ID, Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL));

    String testRefreshToken =
        insertRefreshToken(
            tenant1, TEST_USER_ID, 3600L, SOURCE_VALUE, DEVICE_VALUE, LOCATION_VALUE, IP_ADDRESS);

    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);

    Response authorizeResponse = authorize(tenant1, queryParams);

    String loginChallenge = extractLoginChallenge(authorizeResponse.getHeader(HEADER_LOCATION));

    Map<String, Object> loginAcceptBody = new HashMap<>();
    loginAcceptBody.put(BODY_PARAM_LOGIN_CHALLENGE, loginChallenge);
    loginAcceptBody.put(BODY_PARAM_REFRESH_TOKEN, testRefreshToken);

    Response loginAcceptResponse = loginAccept(tenant1, loginAcceptBody);

    String consentChallenge = extractConsentChallenge(loginAcceptResponse);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CONSENT_CHALLENGE, consentChallenge);
    requestBody.put(BODY_PARAM_CONSENTED_SCOPES, Arrays.asList(SCOPE_OPENID, SCOPE_ADDRESS));
    requestBody.put(BODY_PARAM_REFRESH_TOKEN, testRefreshToken);

    Response response = consentAccept(tenant1, requestBody);

    validateConsentAcceptResponse(
        response,
        tenant1,
        consentChallenge,
        TEST_USER_ID,
        validClientId,
        Arrays.asList(SCOPE_OPENID, SCOPE_ADDRESS, SCOPE_EMAIL));
  }

  @Test
  @DisplayName("Should handle tenant isolation")
  public void testConsentAcceptTenantIsolation() {
    String tenant2RefreshToken =
        insertRefreshToken(
            tenant2, TEST_USER_ID, 3600L, SOURCE_VALUE, DEVICE_VALUE, LOCATION_VALUE, IP_ADDRESS);

    Map<String, Object> requestBody =
        createRequestBody(validConsentChallenge, Arrays.asList(SCOPE_EMAIL), tenant2RefreshToken);

    Response response = consentAccept(tenant2, requestBody);

    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .body(ERROR_FIELD, equalTo(ERROR_UNAUTHORIZED))
        .body(ERROR_DESCRIPTION, equalTo("Invalid challenge"));
  }

  @Test
  @DisplayName("Should handle user mismatch")
  public void testConsentAcceptUserMismatch() {
    String differentUserRefreshToken =
        insertRefreshToken(
            tenant1,
            "different_user",
            3600L,
            SOURCE_VALUE,
            DEVICE_VALUE,
            LOCATION_VALUE,
            IP_ADDRESS);

    Map<String, Object> requestBody =
        createRequestBody(
            validConsentChallenge, Arrays.asList(SCOPE_EMAIL), differentUserRefreshToken);

    Response response = consentAccept(tenant1, requestBody);

    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .body(ERROR_FIELD, equalTo(ERROR_UNAUTHORIZED))
        .body(ERROR_DESCRIPTION, equalTo("Refresh token does not match session user"));
  }

  @Test
  @DisplayName("Should handle expired refresh token")
  public void testConsentAcceptExpiredRefreshToken() {
    String expiredRefreshToken =
        insertRefreshToken(
            tenant1, TEST_USER_ID, -1800L, SOURCE_VALUE, DEVICE_VALUE, LOCATION_VALUE, IP_ADDRESS);

    Map<String, Object> requestBody =
        createRequestBody(validConsentChallenge, Arrays.asList(SCOPE_EMAIL), expiredRefreshToken);

    Response response = consentAccept(tenant1, requestBody);

    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .body(ERROR_FIELD, equalTo(ERROR_UNAUTHORIZED))
        .body(ERROR_DESCRIPTION, equalTo("Invalid refresh token"));
  }

  @Test
  @DisplayName("Should handle multiple concurrent consent accept requests")
  public void testConsentAcceptConcurrentRequests() {
    Map<String, Object> requestBody =
        createRequestBody(
            validConsentChallenge, Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL), validRefreshToken);

    Response response1 = consentAccept(tenant1, requestBody);
    Response response2 = consentAccept(tenant1, requestBody);

    response1.then().statusCode(SC_MOVED_TEMPORARILY).header(HEADER_LOCATION, notNullValue());

    // Verify session was deleted after first successful consent acceptance
    assertThat(
        "Authorize session should be deleted after consent acceptance",
        authorizeSessionExists(tenant1, validConsentChallenge),
        equalTo(false));

    // Validate OIDC code properties for the first successful request
    String authCode = extractAuthCodeFromLocation(response1.getHeader(HEADER_LOCATION));
    if (authCode != null) {
      validateOidcCodeProperties(
          tenant1, authCode, TEST_USER_ID, validClientId, Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL));
    }

    response2
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .body(ERROR_FIELD, equalTo(ERROR_UNAUTHORIZED))
        .body(ERROR_DESCRIPTION, equalTo("Invalid challenge"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"invalid_scope", "nonexistent_scope", "openid_invalid"})
  @DisplayName("Should handle invalid scopes gracefully")
  public void testConsentAcceptInvalidScopes(String invalidScope) {
    Map<String, Object> requestBody =
        createRequestBody(
            validConsentChallenge, Arrays.asList(SCOPE_OPENID, invalidScope), validRefreshToken);

    Response response = consentAccept(tenant1, requestBody);

    validateConsentAcceptResponse(
        response,
        tenant1,
        validConsentChallenge,
        TEST_USER_ID,
        validClientId,
        Arrays.asList(SCOPE_OPENID));
  }

  @Test
  @DisplayName("Should handle mixed valid and invalid scopes")
  public void testConsentAcceptMixedScopes() {
    Map<String, Object> requestBody =
        createRequestBody(
            validConsentChallenge,
            Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, "invalid_scope", SCOPE_PHONE),
            validRefreshToken);

    Response response = consentAccept(tenant1, requestBody);

    validateConsentAcceptResponse(
        response,
        tenant1,
        validConsentChallenge,
        TEST_USER_ID,
        validClientId,
        Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL));
  }

  @Test
  @DisplayName("Should handle duplicate scopes in request")
  public void testConsentAcceptDuplicateScopes() {
    Map<String, Object> requestBody =
        createRequestBody(
            validConsentChallenge,
            Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_EMAIL, SCOPE_PHONE),
            validRefreshToken);

    Response response = consentAccept(tenant1, requestBody);

    validateConsentAcceptResponse(
        response,
        tenant1,
        validConsentChallenge,
        TEST_USER_ID,
        validClientId,
        Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL));
  }

  @Test
  @DisplayName("Should handle all available scopes")
  public void testConsentAcceptAllScopes() {
    Map<String, Object> requestBody =
        createRequestBody(
            validConsentChallenge,
            Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE, SCOPE_ADDRESS),
            validRefreshToken);

    Response response = consentAccept(tenant1, requestBody);

    validateConsentAcceptResponse(
        response,
        tenant1,
        validConsentChallenge,
        TEST_USER_ID,
        validClientId,
        Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS));
  }

  @Test
  @DisplayName("Should return error when openid scope is missing")
  public void testConsentAcceptMissingOpenidScope() {
    Map<String, Object> requestBody =
        createRequestBody(
            validConsentChallenge, Arrays.asList(SCOPE_EMAIL, SCOPE_PHONE), validRefreshToken);

    Response response = consentAccept(tenant1, requestBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo("Atleast openid scope has to be consented"));
  }

  @Test
  @DisplayName("Should handle missing OIDC config")
  public void testConsentAcceptMissingOidcConfig() {
    String nonExistentTenant = "non_existent_tenant";

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CONSENT_CHALLENGE, validConsentChallenge);
    requestBody.put(BODY_PARAM_CONSENTED_SCOPES, Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL));
    requestBody.put(BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    Response response = consentAccept(nonExistentTenant, requestBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("No config found"));
  }

  @Test
  @DisplayName("Should handle expired authorized session")
  public void testConsentAcceptExpiredAuthorizedSession() {
    Map<String, Object> clientRequest = new HashMap<>();
    clientRequest.put("client_name", "Test Expired Session Client");
    clientRequest.put("client_uri", "https://example.com");
    clientRequest.put("redirect_uris", Arrays.asList("https://example.com/callback"));
    clientRequest.put("grant_types", Arrays.asList("authorization_code", "refresh_token"));
    clientRequest.put("response_types", Arrays.asList("code"));
    clientRequest.put("skip_consent", false);

    Response clientResponse = createClient(tenant2, clientRequest);
    String tenant2ClientId = clientResponse.jsonPath().getString(CLIENT_ID);

    createClientScope(
        tenant2,
        tenant2ClientId,
        ClientUtils.createClientScopeRequest(
            Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE, SCOPE_ADDRESS)));

    String refreshToken =
        insertRefreshToken(
            tenant2, TEST_USER_ID, 3600L, SOURCE_VALUE, DEVICE_VALUE, LOCATION_VALUE, IP_ADDRESS);

    Map<String, String> queryParams = createValidAuthorizeRequest(tenant2ClientId);
    Response authorizeResponse = authorize(tenant2, queryParams);

    String loginChallenge = extractLoginChallenge(authorizeResponse.getHeader(HEADER_LOCATION));

    Map<String, Object> loginAcceptBody = new HashMap<>();
    loginAcceptBody.put(BODY_PARAM_LOGIN_CHALLENGE, loginChallenge);
    loginAcceptBody.put(BODY_PARAM_REFRESH_TOKEN, refreshToken);

    Response loginAcceptResponse = loginAccept(tenant2, loginAcceptBody);

    String expiredConsentChallenge = extractConsentChallenge(loginAcceptResponse);

    expireAuthorizeSession(tenant2, expiredConsentChallenge);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CONSENT_CHALLENGE, expiredConsentChallenge);
    requestBody.put(BODY_PARAM_CONSENTED_SCOPES, Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL));
    requestBody.put(BODY_PARAM_REFRESH_TOKEN, refreshToken);

    Response response = consentAccept(tenant2, requestBody);

    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .body(ERROR_FIELD, equalTo(ERROR_UNAUTHORIZED))
        .body(ERROR_DESCRIPTION, equalTo("Invalid challenge"));
  }

  @Test
  @DisplayName("Should verify consents are actually persisted")
  public void testConsentAcceptVerifyConsentsPersisted() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CONSENT_CHALLENGE, validConsentChallenge);
    requestBody.put(
        BODY_PARAM_CONSENTED_SCOPES, Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE));
    requestBody.put(BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    Response response = consentAccept(tenant1, requestBody);

    validateConsentAcceptResponse(
        response,
        tenant1,
        validConsentChallenge,
        TEST_USER_ID,
        validClientId,
        Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL));

    response.then().statusCode(SC_MOVED_TEMPORARILY);
  }

  @Test
  @DisplayName("Should prevent cross-tenant authorized session access")
  public void testConsentAcceptCrossTenantAccess() {
    String tenant2RefreshToken =
        insertRefreshToken(
            tenant2, TEST_USER_ID, 3600L, SOURCE_VALUE, DEVICE_VALUE, LOCATION_VALUE, IP_ADDRESS);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CONSENT_CHALLENGE, validConsentChallenge);
    requestBody.put(BODY_PARAM_CONSENTED_SCOPES, Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL));
    requestBody.put(BODY_PARAM_REFRESH_TOKEN, tenant2RefreshToken);

    Response response = consentAccept(tenant2, requestBody);

    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .body(ERROR_FIELD, equalTo(ERROR_UNAUTHORIZED))
        .body(ERROR_DESCRIPTION, equalTo("Invalid challenge"));
  }

  @Test
  @DisplayName("Should handle cleanup failure gracefully")
  public void testConsentAcceptCleanupFailure() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CONSENT_CHALLENGE, validConsentChallenge);
    requestBody.put(BODY_PARAM_CONSENTED_SCOPES, Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL));
    requestBody.put(BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    Response response = consentAccept(tenant1, requestBody);

    validateConsentAcceptResponse(
        response,
        tenant1,
        validConsentChallenge,
        TEST_USER_ID,
        validClientId,
        Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL));

    response.then().statusCode(SC_MOVED_TEMPORARILY);
  }

  @Test
  @DisplayName("Should handle empty scopes list in insertConsents")
  public void testConsentAcceptEmptyScopesList() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CONSENT_CHALLENGE, validConsentChallenge);
    requestBody.put(BODY_PARAM_CONSENTED_SCOPES, Arrays.asList(SCOPE_OPENID));
    requestBody.put(BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    Response response = consentAccept(tenant1, requestBody);

    validateConsentAcceptResponse(
        response,
        tenant1,
        validConsentChallenge,
        TEST_USER_ID,
        validClientId,
        Arrays.asList(SCOPE_OPENID));
  }

  @Test
  @DisplayName("Should handle database error during consent insertion")
  public void testConsentAcceptDatabaseError() {
    // Use a malformed consent challenge that might cause database errors
    String malformedConsentChallenge = "malformed_challenge_with_special_chars_!@#$%^&*()";

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CONSENT_CHALLENGE, malformedConsentChallenge);
    requestBody.put(BODY_PARAM_CONSENTED_SCOPES, Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL));
    requestBody.put(BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    Response response = consentAccept(tenant1, requestBody);

    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .body(ERROR_FIELD, equalTo(ERROR_UNAUTHORIZED))
        .body(ERROR_DESCRIPTION, equalTo("Invalid challenge"));
  }

  @Test
  @DisplayName("Should handle null consent challenge in async deletion")
  public void testConsentAcceptNullConsentChallenge() {
    // This test ensures the async deletion handles null gracefully
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CONSENT_CHALLENGE, validConsentChallenge);
    requestBody.put(BODY_PARAM_CONSENTED_SCOPES, Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL));
    requestBody.put(BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    Response response = consentAccept(tenant1, requestBody);

    validateConsentAcceptResponse(
        response,
        tenant1,
        validConsentChallenge,
        TEST_USER_ID,
        validClientId,
        Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL));
  }

  @Test
  @DisplayName("Should handle single scope consent insertion")
  public void testConsentAcceptSingleScopeInsertion() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CONSENT_CHALLENGE, validConsentChallenge);
    requestBody.put(BODY_PARAM_CONSENTED_SCOPES, Arrays.asList(SCOPE_OPENID));
    requestBody.put(BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    Response response = consentAccept(tenant1, requestBody);

    validateConsentAcceptResponse(
        response,
        tenant1,
        validConsentChallenge,
        TEST_USER_ID,
        validClientId,
        Arrays.asList(SCOPE_OPENID));
  }

  @Test
  @DisplayName("Should handle multiple scope consent insertion")
  public void testConsentAcceptMultipleScopeInsertion() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CONSENT_CHALLENGE, validConsentChallenge);
    requestBody.put(
        BODY_PARAM_CONSENTED_SCOPES,
        Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE, SCOPE_ADDRESS));
    requestBody.put(BODY_PARAM_REFRESH_TOKEN, validRefreshToken);

    Response response = consentAccept(tenant1, requestBody);

    validateConsentAcceptResponse(
        response,
        tenant1,
        validConsentChallenge,
        TEST_USER_ID,
        validClientId,
        Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS));
  }

  @Test
  @DisplayName("Should handle batch consent insertion with existing consents")
  public void testConsentAcceptBatchInsertionWithExisting() {
    // Insert some existing consents
    insertUserConsent(
        tenant1, validClientId, TEST_USER_ID, Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL));

    // Create test data for this specific test
    String testRefreshToken =
        insertRefreshToken(
            tenant1, TEST_USER_ID, 3600L, SOURCE_VALUE, DEVICE_VALUE, LOCATION_VALUE, IP_ADDRESS);

    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    Response authorizeResponse = authorize(tenant1, queryParams);

    String loginChallenge = extractLoginChallenge(authorizeResponse.getHeader(HEADER_LOCATION));

    Map<String, Object> loginAcceptBody = new HashMap<>();
    loginAcceptBody.put(BODY_PARAM_LOGIN_CHALLENGE, loginChallenge);
    loginAcceptBody.put(BODY_PARAM_REFRESH_TOKEN, testRefreshToken);

    Response loginAcceptResponse = loginAccept(tenant1, loginAcceptBody);

    String consentChallenge = extractConsentChallenge(loginAcceptResponse);

    // Add more scopes
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BODY_PARAM_CONSENT_CHALLENGE, consentChallenge);
    requestBody.put(
        BODY_PARAM_CONSENTED_SCOPES, Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS));
    requestBody.put(BODY_PARAM_REFRESH_TOKEN, testRefreshToken);

    Response response = consentAccept(tenant1, requestBody);

    validateConsentAcceptResponse(
        response,
        tenant1,
        consentChallenge,
        TEST_USER_ID,
        validClientId,
        Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS));
  }
}

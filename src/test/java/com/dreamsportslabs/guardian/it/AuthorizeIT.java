package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.AUTH_CODE_CHALLENGE_METHOD_INVALID;
import static com.dreamsportslabs.guardian.Constants.AUTH_CODE_CHALLENGE_METHOD_PLAIN;
import static com.dreamsportslabs.guardian.Constants.AUTH_CODE_CHALLENGE_METHOD_S256;
import static com.dreamsportslabs.guardian.Constants.AUTH_PROMPT_INVALID;
import static com.dreamsportslabs.guardian.Constants.AUTH_PROMPT_LOGIN;
import static com.dreamsportslabs.guardian.Constants.AUTH_RESPONSE_TYPE_TOKEN;
import static com.dreamsportslabs.guardian.Constants.CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.ERROR_CLIENT_AUTHENTICATION_FAILED;
import static com.dreamsportslabs.guardian.Constants.ERROR_CLIENT_ID_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.ERROR_CODE_CHALLENGE_TOGETHER;
import static com.dreamsportslabs.guardian.Constants.ERROR_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.ERROR_DESC_PARAM_FORMAT;
import static com.dreamsportslabs.guardian.Constants.ERROR_FIELD;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_CLIENT;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_REDIRECT_URI;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_SCOPE;
import static com.dreamsportslabs.guardian.Constants.ERROR_PARAM_FORMAT;
import static com.dreamsportslabs.guardian.Constants.ERROR_REDIRECT_URI_INVALID;
import static com.dreamsportslabs.guardian.Constants.ERROR_REDIRECT_URI_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.ERROR_RESPONSE_TYPE_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.ERROR_SCOPE_MUST_CONTAIN_OPENID;
import static com.dreamsportslabs.guardian.Constants.ERROR_SCOPE_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.HEADER_LOCATION;
import static com.dreamsportslabs.guardian.Constants.INVALID_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.INVALID_TENANT;
import static com.dreamsportslabs.guardian.Constants.LOGIN_CHALLENGE_PARAM;
import static com.dreamsportslabs.guardian.Constants.LOGIN_HINT_PARAM_FORMAT;
import static com.dreamsportslabs.guardian.Constants.LOGIN_PAGE_URL;
import static com.dreamsportslabs.guardian.Constants.MALICIOUS_CALLBACK_URL;
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
import static com.dreamsportslabs.guardian.Constants.PROMPT_PARAM_FORMAT;
import static com.dreamsportslabs.guardian.Constants.SCOPE_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.SCOPE_EMAIL;
import static com.dreamsportslabs.guardian.Constants.SCOPE_OPENID;
import static com.dreamsportslabs.guardian.Constants.SCOPE_PHONE;
import static com.dreamsportslabs.guardian.Constants.STATE_PARAM_FORMAT;
import static com.dreamsportslabs.guardian.Constants.TENANT_1;
import static com.dreamsportslabs.guardian.Constants.TENANT_2;
import static com.dreamsportslabs.guardian.Constants.TEST_CODE_CHALLENGE;
import static com.dreamsportslabs.guardian.Constants.TEST_LOGIN_HINT;
import static com.dreamsportslabs.guardian.Constants.TEST_NONCE;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.authorize;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createClient;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createClientScope;
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
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class AuthorizeIT {

  public static String tenant1 = TENANT_1;
  public static String tenant2 = TENANT_2;

  private String validClientId;

  @BeforeEach
  void setUp() {
    // Clean up any existing test data
    cleanupClients(tenant1);
    cleanupClients(tenant2);

    // Create a test client for authorization tests
    Response clientResponse = createTestClient();
    validClientId = clientResponse.jsonPath().getString(CLIENT_ID);

    // Using ApplicationIoUtils directly (alternative approach)
    createClientScope(
        tenant1,
        validClientId,
        ClientUtils.createClientScopeRequest(
            SCOPE_OPENID, SCOPE_EMAIL, SCOPE_ADDRESS, SCOPE_PHONE));
  }

  @Test
  @DisplayName("Should authorize successfully with valid parameters")
  public void testAuthorizeSuccess() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(PARAM_PROMPT, AUTH_PROMPT_LOGIN);
    queryParams.put(PARAM_LOGIN_HINT, TEST_LOGIN_HINT);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate
    response
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue())
        .header(HEADER_LOCATION, containsString(LOGIN_CHALLENGE_PARAM))
        .header(
            HEADER_LOCATION,
            containsString(
                String.format(
                    STATE_PARAM_FORMAT,
                    URLEncoder.encode(queryParams.get(PARAM_STATE), StandardCharsets.UTF_8))))
        .header(
            HEADER_LOCATION,
            containsString(
                String.format(
                    PROMPT_PARAM_FORMAT,
                    URLEncoder.encode(queryParams.get(PARAM_PROMPT), StandardCharsets.UTF_8))))
        .header(HEADER_LOCATION, startsWith(LOGIN_PAGE_URL));

    // Verify session was created in redis
    String location = response.getHeader(HEADER_LOCATION);
    String loginChallenge = extractLoginChallenge(location);
    assertThat(authorizeSessionExists(tenant1, loginChallenge), equalTo(true));
  }

  @Test
  @DisplayName("Should return error when client_id is missing")
  public void testAuthorizeMissingClientId() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.remove(PARAM_CLIENT_ID);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate - Basic validation errors follow OIDC JSON format (400 JSON)
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo(ERROR_CLIENT_ID_REQUIRED));
  }

  @Test
  @DisplayName("Should return error when scope is missing")
  public void testAuthorizeMissingScope() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.remove(PARAM_SCOPE);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate - Basic validation errors follow OIDC JSON format (400 JSON)
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo(ERROR_SCOPE_REQUIRED));
  }

  @Test
  @DisplayName("Should return error when redirect_uri is missing")
  public void testAuthorizeMissingRedirectUri() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.remove(PARAM_REDIRECT_URI);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate - Basic validation errors follow OIDC JSON format (400 JSON)
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo(ERROR_REDIRECT_URI_REQUIRED));
  }

  @Test
  @DisplayName("Should return error when response_type is missing")
  public void testAuthorizeMissingResponseType() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.remove(PARAM_RESPONSE_TYPE);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate - Basic validation errors follow OIDC JSON format (400 JSON)
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo(ERROR_RESPONSE_TYPE_REQUIRED));
  }

  @Test
  @DisplayName("Should return error when client_id is invalid")
  public void testAuthorizeInvalidClientId() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(PARAM_CLIENT_ID, INVALID_CLIENT_ID);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate - Client authentication errors return 400 JSON response
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_CLIENT))
        .body(ERROR_DESCRIPTION, equalTo(ERROR_CLIENT_AUTHENTICATION_FAILED));
  }

  @Test
  @DisplayName("Should return error when redirect_uri is not registered")
  public void testAuthorizeInvalidRedirectUri() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(PARAM_REDIRECT_URI, MALICIOUS_CALLBACK_URL);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate - Basic validation errors follow OIDC JSON format (400 JSON)
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_REDIRECT_URI))
        .body(ERROR_DESCRIPTION, equalTo(ERROR_REDIRECT_URI_INVALID));
  }

  @Test
  @DisplayName("Should return error when response_type is invalid")
  public void testAuthorizeInvalidResponseType() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(PARAM_RESPONSE_TYPE, AUTH_RESPONSE_TYPE_TOKEN);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate - OIDC JSON format with flat error and error_description fields
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_REQUEST))
        .body(
            ERROR_DESCRIPTION,
            equalTo("Unsupported response_type: '" + AUTH_RESPONSE_TYPE_TOKEN + "'"));
  }

  @Test
  @DisplayName("Should return error when scope does not contain openid")
  public void testAuthorizeScopeWithoutOpenid() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(PARAM_SCOPE, SCOPE_EMAIL + " " + SCOPE_ADDRESS);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate - OIDC errors are returned as 302 redirects with error parameters
    response
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue())
        .header(
            HEADER_LOCATION, containsString(String.format(ERROR_PARAM_FORMAT, ERROR_INVALID_SCOPE)))
        .header(
            HEADER_LOCATION,
            containsString(
                String.format(
                    ERROR_DESC_PARAM_FORMAT,
                    URLEncoder.encode(ERROR_SCOPE_MUST_CONTAIN_OPENID, StandardCharsets.UTF_8))))
        .header(
            HEADER_LOCATION,
            containsString(String.format(STATE_PARAM_FORMAT, queryParams.get(PARAM_STATE))))
        .header(HEADER_LOCATION, startsWith(queryParams.get(PARAM_REDIRECT_URI)));
  }

  @Test
  @DisplayName("Should return error when tenant-id header is missing")
  public void testAuthorizeMissingTenantId() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);

    // Act
    Response response = authorize(null, queryParams);

    // Validate - Missing tenant-id should result in a 400 error
    response.then().statusCode(SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return error when non-existent tenant-id is provided")
  public void testAuthorizeNonExistentTenantId() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);

    // Act
    Response response = authorize(INVALID_TENANT, queryParams);

    // Validate - Invalid tenant should result in a 400 error
    response.then().statusCode(SC_BAD_REQUEST);
  }

  @ParameterizedTest
  @ValueSource(strings = {AUTH_CODE_CHALLENGE_METHOD_S256, AUTH_CODE_CHALLENGE_METHOD_PLAIN})
  @DisplayName("Should handle PKCE with different challenge methods")
  public void testAuthorizeWithPkce(String codeChallengeMethod) {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(PARAM_CODE_CHALLENGE, TEST_CODE_CHALLENGE);
    queryParams.put(PARAM_CODE_CHALLENGE_METHOD, codeChallengeMethod);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate
    response
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue())
        .header(HEADER_LOCATION, containsString(LOGIN_CHALLENGE_PARAM))
        .header(
            HEADER_LOCATION,
            containsString(String.format(STATE_PARAM_FORMAT, queryParams.get(PARAM_STATE))))
        .header(HEADER_LOCATION, startsWith(LOGIN_PAGE_URL));

    // Verify session was created in redis
    String location = response.getHeader(HEADER_LOCATION);
    String loginChallenge = extractLoginChallenge(location);
    assertThat(authorizeSessionExists(tenant1, loginChallenge), equalTo(true));
  }

  @Test
  @DisplayName("Should return error when code_challenge is provided without method")
  public void testAuthorizeCodeChallengeWithoutMethod() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(PARAM_CODE_CHALLENGE, TEST_CODE_CHALLENGE);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate - Basic validation errors follow OIDC JSON format (400 JSON)
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo(ERROR_CODE_CHALLENGE_TOGETHER));
  }

  @Test
  @DisplayName("Should return error when code_challenge_method is provided without challenge")
  public void testAuthorizeCodeChallengeMethodWithoutChallenge() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(PARAM_CODE_CHALLENGE_METHOD, AUTH_CODE_CHALLENGE_METHOD_S256);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate - Basic validation errors follow OIDC JSON format (400 JSON)
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo(ERROR_CODE_CHALLENGE_TOGETHER));
  }

  @Test
  @DisplayName("Should return error when code_challenge_method is invalid")
  public void testAuthorizeInvalidCodeChallengeMethod() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(PARAM_CODE_CHALLENGE, TEST_CODE_CHALLENGE);
    queryParams.put(PARAM_CODE_CHALLENGE_METHOD, AUTH_CODE_CHALLENGE_METHOD_INVALID);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate - OIDC JSON format with flat error and error_description fields
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_REQUEST))
        .body(
            ERROR_DESCRIPTION,
            equalTo(
                "Unsupported code_challenge_method: '" + AUTH_CODE_CHALLENGE_METHOD_INVALID + "'"));
  }

  @Test
  @DisplayName("Should handle multiple scopes correctly")
  public void testAuthorizeMultipleScopes() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(
        PARAM_SCOPE, SCOPE_OPENID + " " + SCOPE_EMAIL + " " + SCOPE_ADDRESS + " " + SCOPE_PHONE);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate
    response
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue())
        .header(HEADER_LOCATION, containsString(LOGIN_CHALLENGE_PARAM))
        .header(
            HEADER_LOCATION,
            containsString(String.format(STATE_PARAM_FORMAT, queryParams.get(PARAM_STATE))))
        .header(HEADER_LOCATION, startsWith(LOGIN_PAGE_URL));

    // Verify session was created in redis
    String location = response.getHeader(HEADER_LOCATION);
    String loginChallenge = extractLoginChallenge(location);
    assertThat(authorizeSessionExists(tenant1, loginChallenge), equalTo(true));
  }

  @Test
  @DisplayName("Should handle optional parameters correctly")
  public void testAuthorizeWithOptionalParameters() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(PARAM_NONCE, TEST_NONCE);
    queryParams.put(PARAM_LOGIN_HINT, TEST_LOGIN_HINT);
    queryParams.put(PARAM_PROMPT, AUTH_PROMPT_LOGIN);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate
    response
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue())
        .header(HEADER_LOCATION, containsString(LOGIN_CHALLENGE_PARAM))
        .header(
            HEADER_LOCATION,
            containsString(String.format(STATE_PARAM_FORMAT, queryParams.get(PARAM_STATE))))
        .header(
            HEADER_LOCATION,
            containsString(
                String.format(
                    PROMPT_PARAM_FORMAT,
                    URLEncoder.encode(queryParams.get(PARAM_PROMPT), StandardCharsets.UTF_8))))
        .header(
            HEADER_LOCATION,
            containsString(
                String.format(
                    LOGIN_HINT_PARAM_FORMAT,
                    URLEncoder.encode(queryParams.get(PARAM_LOGIN_HINT), StandardCharsets.UTF_8))))
        .header(HEADER_LOCATION, startsWith(LOGIN_PAGE_URL));

    // Verify session was created in redis
    String location = response.getHeader(HEADER_LOCATION);
    String loginChallenge = extractLoginChallenge(location);
    assertThat(authorizeSessionExists(tenant1, loginChallenge), equalTo(true));
  }

  @Test
  @DisplayName("Should handle only openid scope gracefully")
  public void testAuthorizeOnlyOpenidScope() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(PARAM_SCOPE, SCOPE_OPENID);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate
    response
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue())
        .header(HEADER_LOCATION, containsString(LOGIN_CHALLENGE_PARAM))
        .header(
            HEADER_LOCATION,
            containsString(String.format(STATE_PARAM_FORMAT, queryParams.get(PARAM_STATE))))
        .header(HEADER_LOCATION, startsWith(LOGIN_PAGE_URL));

    // Verify session was created in redis
    String location = response.getHeader(HEADER_LOCATION);
    String loginChallenge = extractLoginChallenge(location);
    assertThat(authorizeSessionExists(tenant1, loginChallenge), equalTo(true));
  }

  @Test
  @DisplayName("Should handle very long state parameter")
  public void testAuthorizeLongStateParameter() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    String longState = RandomStringUtils.randomAlphanumeric(1000);
    queryParams.put(PARAM_STATE, longState);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate
    response
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue())
        .header(HEADER_LOCATION, containsString(LOGIN_CHALLENGE_PARAM))
        .header(
            HEADER_LOCATION,
            containsString(
                String.format(
                    STATE_PARAM_FORMAT, URLEncoder.encode(longState, StandardCharsets.UTF_8))))
        .header(HEADER_LOCATION, startsWith(LOGIN_PAGE_URL));

    // Verify session was created in redis
    String location = response.getHeader(HEADER_LOCATION);
    String loginChallenge = extractLoginChallenge(location);
    assertThat(authorizeSessionExists(tenant1, loginChallenge), equalTo(true));
  }

  @Test
  @DisplayName("Should return error for invalid prompt value")
  public void testAuthorizeWithInvalidPromptValue() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(PARAM_PROMPT, AUTH_PROMPT_INVALID);
    queryParams.put(PARAM_LOGIN_HINT, TEST_LOGIN_HINT);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate - Basic validation errors follow OIDC JSON format (400 JSON)
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo("Unsupported prompt: '" + AUTH_PROMPT_INVALID + "'"));
  }

  @Test
  @DisplayName("Should handle multiple scopes with some valid and some invalid")
  public void testAuthorizeMultipleScopesWithMixedValidity() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(
        PARAM_SCOPE, SCOPE_OPENID + " " + SCOPE_EMAIL + " " + "invalid_scope" + " " + SCOPE_PHONE);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate - Should succeed but only with valid scopes
    response
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue())
        .header(HEADER_LOCATION, containsString(LOGIN_CHALLENGE_PARAM))
        .header(
            HEADER_LOCATION,
            containsString(String.format(STATE_PARAM_FORMAT, queryParams.get(PARAM_STATE))))
        .header(HEADER_LOCATION, startsWith(LOGIN_PAGE_URL));

    // Verify session was created in redis
    String location = response.getHeader(HEADER_LOCATION);
    String loginChallenge = extractLoginChallenge(location);
    assertThat(authorizeSessionExists(tenant1, loginChallenge), equalTo(true));
  }

  @Test
  @DisplayName("Should handle multiple scopes with all invalid except openid")
  public void testAuthorizeMultipleScopesWithAllInvalidExceptOpenid() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(
        PARAM_SCOPE,
        SCOPE_OPENID + " " + "invalid_scope1" + " " + "invalid_scope2" + " " + "invalid_scope3");

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate - Should succeed with only openid scope
    response
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue())
        .header(HEADER_LOCATION, containsString(LOGIN_CHALLENGE_PARAM))
        .header(
            HEADER_LOCATION,
            containsString(String.format(STATE_PARAM_FORMAT, queryParams.get(PARAM_STATE))))
        .header(HEADER_LOCATION, startsWith(LOGIN_PAGE_URL));

    // Verify session was created in redis
    String location = response.getHeader(HEADER_LOCATION);
    String loginChallenge = extractLoginChallenge(location);
    assertThat(authorizeSessionExists(tenant1, loginChallenge), equalTo(true));
  }

  @ParameterizedTest
  @ValueSource(strings = {"a", "ab", "abc"})
  @DisplayName("Should reject code_challenge that is too short")
  public void testAuthorizeCodeChallengeTooShort(String shortCodeChallenge) {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(PARAM_CODE_CHALLENGE, shortCodeChallenge);
    queryParams.put(PARAM_CODE_CHALLENGE_METHOD, AUTH_CODE_CHALLENGE_METHOD_S256);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo("code_challenge must be between 43 and 128 characters"));
  }

  @Test
  @DisplayName("Should reject code_challenge that is too long")
  public void testAuthorizeCodeChallengeTooLong() {
    // Arrange
    String longCodeChallenge = RandomStringUtils.randomAlphanumeric(129);
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(PARAM_CODE_CHALLENGE, longCodeChallenge);
    queryParams.put(PARAM_CODE_CHALLENGE_METHOD, AUTH_CODE_CHALLENGE_METHOD_S256);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo("code_challenge must be between 43 and 128 characters"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "invalid-base64!",
        "not-base64@#$",
        "contains spaces",
        "has\nnewline",
        "has\ttab",
        "invalid#chars",
        "invalid$chars",
        "invalid%chars"
      })
  @DisplayName("Should reject code_challenge with invalid base64url encoding")
  public void testAuthorizeCodeChallengeInvalidBase64Url(String invalidCodeChallenge) {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(PARAM_CODE_CHALLENGE, invalidCodeChallenge);
    queryParams.put(PARAM_CODE_CHALLENGE_METHOD, AUTH_CODE_CHALLENGE_METHOD_S256);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_REQUEST))
        .body(
            ERROR_DESCRIPTION,
            equalTo("code_challenge must contain only base64url characters (A-Z, a-z, 0-9, -, _)"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "http://malicious.com/callback",
        "ftp://malicious.com/callback",
        "file:///etc/passwd"
      })
  @DisplayName("Should reject malformed redirect_uri")
  public void testAuthorizeMalformedRedirectUri(String malformedRedirectUri) {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(PARAM_REDIRECT_URI, malformedRedirectUri);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate - Print response for debugging
    System.out.println("Response: " + response.getBody().asString());
    response.then().statusCode(SC_BAD_REQUEST);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"token", "id_token", "token id_token", "code token", "code id_token token"})
  @DisplayName("Should reject unsupported response_type values")
  public void testAuthorizeUnsupportedResponseType(String unsupportedResponseType) {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(PARAM_RESPONSE_TYPE, unsupportedResponseType);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_REQUEST))
        .body(
            ERROR_DESCRIPTION,
            equalTo("Unsupported response_type: '" + unsupportedResponseType + "'"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"   ", "\t", "\n", "  \t  \n  ", " email", " openid email"})
  @DisplayName("Should reject scope with empty values and whitespace")
  public void testAuthorizeScopeWithEmptyValues(String scopeWithEmptyValues) {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(PARAM_SCOPE, scopeWithEmptyValues);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate - Print response for debugging
    System.out.println("Scope test response: " + response.getBody().asString());
    response.then().statusCode(SC_BAD_REQUEST);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"openid!email", "openid@email", "openid#email", "openid$email", "openid%email"})
  @DisplayName("Should reject scope with invalid characters")
  public void testAuthorizeScopeWithInvalidCharacters(String invalidScope) {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(PARAM_SCOPE, invalidScope);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate - OIDC errors are returned as 302 redirects with error parameters
    response
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue())
        .header(
            HEADER_LOCATION, containsString(String.format(ERROR_PARAM_FORMAT, ERROR_INVALID_SCOPE)))
        .header(
            HEADER_LOCATION,
            containsString(
                String.format(
                    ERROR_DESC_PARAM_FORMAT,
                    URLEncoder.encode(ERROR_SCOPE_MUST_CONTAIN_OPENID, StandardCharsets.UTF_8))))
        .header(
            HEADER_LOCATION,
            containsString(String.format(STATE_PARAM_FORMAT, queryParams.get(PARAM_STATE))))
        .header(HEADER_LOCATION, startsWith(queryParams.get(PARAM_REDIRECT_URI)));
  }

  @Test
  @DisplayName("Should not access the client from a different tenant")
  public void testAuthorizeCrossTenantClientAccess() {
    // Arrange - Create client in tenant1, try to access from tenant2
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);

    // Act
    Response response = authorize(tenant2, queryParams);

    // Validate - Should fail with client authentication error
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .body(ERROR_FIELD, equalTo(ERROR_INVALID_CLIENT))
        .body(ERROR_DESCRIPTION, equalTo(ERROR_CLIENT_AUTHENTICATION_FAILED));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @DisplayName("Should handle null query parameters gracefully")
  public void testAuthorizeNullQueryParameters(String nullValue) {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    // Remove all parameters to simulate null/empty request
    queryParams.clear();

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate - Should fail with missing required parameters
    response.then().statusCode(SC_BAD_REQUEST).body(ERROR_FIELD, equalTo(ERROR_INVALID_REQUEST));
  }

  @Test
  @DisplayName("Should create unique login challenges for concurrent requests")
  public void testAuthorizeConcurrentRequestsUniqueChallenges() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);

    // Act - Make multiple concurrent requests
    Response response1 = authorize(tenant1, queryParams);
    Response response2 = authorize(tenant1, queryParams);
    Response response3 = authorize(tenant1, queryParams);

    // Validate - All should succeed with unique login challenges
    response1
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue())
        .header(HEADER_LOCATION, containsString(LOGIN_CHALLENGE_PARAM));

    response2
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue())
        .header(HEADER_LOCATION, containsString(LOGIN_CHALLENGE_PARAM));

    response3
        .then()
        .statusCode(SC_MOVED_TEMPORARILY)
        .header(HEADER_LOCATION, notNullValue())
        .header(HEADER_LOCATION, containsString(LOGIN_CHALLENGE_PARAM));

    // Verify all login challenges are unique
    String loginChallenge1 = extractLoginChallenge(response1.getHeader(HEADER_LOCATION));
    String loginChallenge2 = extractLoginChallenge(response2.getHeader(HEADER_LOCATION));
    String loginChallenge3 = extractLoginChallenge(response3.getHeader(HEADER_LOCATION));

    assertThat(loginChallenge1, notNullValue());
    assertThat(loginChallenge2, notNullValue());
    assertThat(loginChallenge3, notNullValue());
    assertThat(loginChallenge1.equals(loginChallenge2), equalTo(false));
    assertThat(loginChallenge1.equals(loginChallenge3), equalTo(false));
    assertThat(loginChallenge2.equals(loginChallenge3), equalTo(false));
  }

  private Response createTestClient() {
    Map<String, Object> requestBody = ClientUtils.createValidClientRequest();
    return createClient(tenant1, requestBody);
  }
}

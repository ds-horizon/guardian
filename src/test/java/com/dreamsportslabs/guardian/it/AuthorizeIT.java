package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.AUTH_CODE_CHALLENGE_METHOD_INVALID;
import static com.dreamsportslabs.guardian.Constants.AUTH_CODE_CHALLENGE_METHOD_S256;
import static com.dreamsportslabs.guardian.Constants.AUTH_PROMPT_INVALID;
import static com.dreamsportslabs.guardian.Constants.AUTH_PROMPT_LOGIN;
import static com.dreamsportslabs.guardian.Constants.AUTH_RESPONSE_TYPE_TOKEN;
import static com.dreamsportslabs.guardian.Constants.AUTH_STATE_SPECIAL_CHARS;
import static com.dreamsportslabs.guardian.Constants.CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
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
import static org.hamcrest.Matchers.stringContainsInOrder;

import com.dreamsportslabs.guardian.utils.ClientUtils;
import io.restassured.response.Response;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

    // Verify session was created in database
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

    // Validate - Basic validation errors follow AuthorizeRequestDto format (400 JSON)
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_CLIENT_ID_REQUIRED));
  }

  @Test
  @DisplayName("Should return error when scope is missing")
  public void testAuthorizeMissingScope() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.remove(PARAM_SCOPE);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate - Basic validation errors follow AuthorizeRequestDto format (400 JSON)
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_SCOPE_REQUIRED));
  }

  @Test
  @DisplayName("Should return error when redirect_uri is missing")
  public void testAuthorizeMissingRedirectUri() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.remove(PARAM_REDIRECT_URI);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate - Basic validation errors follow AuthorizeRequestDto format (400 JSON)
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_REDIRECT_URI_REQUIRED));
  }

  @Test
  @DisplayName("Should return error when response_type is missing")
  public void testAuthorizeMissingResponseType() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.remove(PARAM_RESPONSE_TYPE);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate - Basic validation errors follow AuthorizeRequestDto format (400 JSON)
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_RESPONSE_TYPE_REQUIRED));
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
        .statusCode(SC_BAD_REQUEST)
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

    // Validate - Basic validation errors follow AuthorizeRequestDto format (400 JSON)
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(
            MESSAGE,
            stringContainsInOrder(
                Arrays.asList(
                    "The value provided for the field is invalid or does not exist:",
                    "response_type")));
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

  @Test
  @DisplayName("Should handle PKCE code challenge and method")
  public void testAuthorizeWithPkce() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(PARAM_CODE_CHALLENGE, TEST_CODE_CHALLENGE);
    queryParams.put(PARAM_CODE_CHALLENGE_METHOD, AUTH_CODE_CHALLENGE_METHOD_S256);

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

    // Verify session was created in database
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

    // Validate - Basic validation errors follow AuthorizeRequestDto format (400 JSON)
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_CODE_CHALLENGE_TOGETHER));
  }

  @Test
  @DisplayName("Should return error when code_challenge_method is provided without challenge")
  public void testAuthorizeCodeChallengeMethodWithoutChallenge() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(PARAM_CODE_CHALLENGE_METHOD, AUTH_CODE_CHALLENGE_METHOD_S256);

    // Act
    Response response = authorize(tenant1, queryParams);

    // Validate - Basic validation errors follow AuthorizeRequestDto format (400 JSON)
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_CODE_CHALLENGE_TOGETHER));
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

    // Validate - Basic validation errors follow AuthorizeRequestDto format (400 JSON)
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(
            MESSAGE,
            stringContainsInOrder(
                Arrays.asList(
                    "The value provided for the field is invalid or does not exist:",
                    "code_challenge_method")));
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

    // Verify session was created in database
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

    // Verify session was created in database
    String location = response.getHeader(HEADER_LOCATION);
    String loginChallenge = extractLoginChallenge(location);
    assertThat(authorizeSessionExists(tenant1, loginChallenge), equalTo(true));
  }

  @Test
  @DisplayName("Should handle empty scope list gracefully")
  public void testAuthorizeEmptyScopeList() {
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

    // Verify session was created in database
    String location = response.getHeader(HEADER_LOCATION);
    String loginChallenge = extractLoginChallenge(location);
    assertThat(authorizeSessionExists(tenant1, loginChallenge), equalTo(true));
  }

  @Test
  @DisplayName("Should handle special characters in state parameter")
  public void testAuthorizeSpecialCharactersInState() {
    // Arrange
    Map<String, String> queryParams = createValidAuthorizeRequest(validClientId);
    queryParams.put(PARAM_STATE, AUTH_STATE_SPECIAL_CHARS);

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
                    URLEncoder.encode(AUTH_STATE_SPECIAL_CHARS, StandardCharsets.UTF_8))))
        .header(HEADER_LOCATION, startsWith(LOGIN_PAGE_URL));

    // Verify session was created in database
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

    // Verify session was created in database
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

    // Validate - Basic validation errors follow AuthorizeRequestDto format (400 JSON)
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(
            MESSAGE,
            stringContainsInOrder(
                Arrays.asList(
                    "The value provided for the field is invalid or does not exist:", "prompt")));
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

    // Verify session was created in database
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

    // Verify session was created in database
    String location = response.getHeader(HEADER_LOCATION);
    String loginChallenge = extractLoginChallenge(location);
    assertThat(authorizeSessionExists(tenant1, loginChallenge), equalTo(true));
  }

  private Response createTestClient() {
    Map<String, Object> requestBody = ClientUtils.createValidClientRequest();
    return createClient(tenant1, requestBody);
  }
}

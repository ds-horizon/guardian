package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.AUTHORIZATION_CODE;
import static com.dreamsportslabs.guardian.Constants.AUTH_BASIC_PREFIX;
import static com.dreamsportslabs.guardian.Constants.AUTH_TEST_CLIENT_NAME;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CLAIMS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_IS_OIDC;
import static com.dreamsportslabs.guardian.Constants.CLAIM_SUB;
import static com.dreamsportslabs.guardian.Constants.CLIENT_CREDENTIALS;
import static com.dreamsportslabs.guardian.Constants.CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.CLIENT_NAME;
import static com.dreamsportslabs.guardian.Constants.CLIENT_SECRET;
import static com.dreamsportslabs.guardian.Constants.CONTENT_TYPE_FORM_URLENCODED;
import static com.dreamsportslabs.guardian.Constants.DISPLAY_NAME;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_DESCRIPTION;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_CLIENT;
import static com.dreamsportslabs.guardian.Constants.GRANT_TYPES;
import static com.dreamsportslabs.guardian.Constants.HEADER_AUTHORIZATION;
import static com.dreamsportslabs.guardian.Constants.HEADER_CONTENT_TYPE;
import static com.dreamsportslabs.guardian.Constants.INVALID_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.INVALID_CLIENT_SECRET;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.REFRESH_TOKEN_EXPIRY_SECONDS;
import static com.dreamsportslabs.guardian.Constants.SCOPE;
import static com.dreamsportslabs.guardian.Constants.SCOPE_EMAIL;
import static com.dreamsportslabs.guardian.Constants.SCOPE_OPENID;
import static com.dreamsportslabs.guardian.Constants.SCOPE_PHONE;
import static com.dreamsportslabs.guardian.Constants.TENANT_1;
import static com.dreamsportslabs.guardian.Constants.TEST_DEVICE_NAME;
import static com.dreamsportslabs.guardian.Constants.TEST_EMAIL_CLAIM;
import static com.dreamsportslabs.guardian.Constants.TEST_EMAIL_VERIFIED_CLAIM;
import static com.dreamsportslabs.guardian.Constants.TEST_IP_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.TEST_PHONE_CLAIM;
import static com.dreamsportslabs.guardian.Constants.TEST_PHONE_VERIFIED_CLAIM;
import static com.dreamsportslabs.guardian.Constants.TEST_USER_ID;
import static com.dreamsportslabs.guardian.Constants.TOKEN;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_INVALID_CLIENT;
import static com.dreamsportslabs.guardian.Constants.TOKEN_ERROR_MSG_CLIENT_AUTH_FAILED;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createClient;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createClientScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createScope;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.revokeToken;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupClients;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupOidcRefreshTokens;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupRedis;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupScopes;
import static com.dreamsportslabs.guardian.utils.DbUtils.insertOidcRefreshToken;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dreamsportslabs.guardian.utils.ClientUtils;
import com.dreamsportslabs.guardian.utils.DbUtils;
import io.restassured.response.Response;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RevokeTokenIT {

  private static final String tenant1 = TENANT_1;
  private static String validClientId;
  private static String validClientSecret;

  @BeforeAll
  static void cleanupTestData() {
    cleanupClients(tenant1);
    cleanupScopes(tenant1);
    cleanupOidcRefreshTokens(tenant1);
    cleanupRedis();

    createRequiredScopes(tenant1);

    // Create a test client for revocation tests
    Response clientResponse = createTestClient();
    validClientId = clientResponse.jsonPath().getString(CLIENT_ID);
    validClientSecret = clientResponse.jsonPath().getString(CLIENT_SECRET);

    // Associate scopes with the client
    createClientScope(
        tenant1,
        validClientId,
        ClientUtils.createClientScopeRequest(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE));
  }

  @BeforeEach
  void setUp() {
    cleanupOidcRefreshTokens(tenant1);
    cleanupRedis();
  }

  private static Response createTestClient() {
    Map<String, Object> requestBody = ClientUtils.createValidClientRequest();
    requestBody.put(
        GRANT_TYPES, Arrays.asList(AUTHORIZATION_CODE, CLIENT_CREDENTIALS, REFRESH_TOKEN));
    return createClient(tenant1, requestBody);
  }

  private static void createRequiredScopes(String tenantId) {
    Map<String, Object> openidScope = new HashMap<>();
    openidScope.put(SCOPE, SCOPE_OPENID);
    openidScope.put(DISPLAY_NAME, SCOPE_OPENID);
    openidScope.put(BODY_PARAM_CLAIMS, Arrays.asList(CLAIM_SUB));
    openidScope.put(BODY_PARAM_IS_OIDC, true);
    createScope(tenantId, openidScope);

    Map<String, Object> emailScope = new HashMap<>();
    emailScope.put(SCOPE, SCOPE_EMAIL);
    emailScope.put(DISPLAY_NAME, SCOPE_EMAIL);
    emailScope.put(BODY_PARAM_CLAIMS, Arrays.asList(TEST_EMAIL_CLAIM, TEST_EMAIL_VERIFIED_CLAIM));
    emailScope.put(BODY_PARAM_IS_OIDC, true);
    createScope(tenantId, emailScope);

    Map<String, Object> phoneScope = new HashMap<>();
    phoneScope.put(SCOPE, SCOPE_PHONE);
    phoneScope.put(DISPLAY_NAME, SCOPE_EMAIL);
    phoneScope.put(BODY_PARAM_CLAIMS, Arrays.asList(TEST_PHONE_CLAIM, TEST_PHONE_VERIFIED_CLAIM));
    phoneScope.put(BODY_PARAM_IS_OIDC, true);
    createScope(tenantId, phoneScope);
  }

  private String getBasicAuthHeader(String clientId, String clientSecret) {
    String clientCredentials = clientId + ":" + clientSecret;
    String authHeader =
        new String(Base64.getEncoder().encode(clientCredentials.getBytes(StandardCharsets.UTF_8)));
    return AUTH_BASIC_PREFIX + authHeader;
  }

  @Test
  @DisplayName("Should revoke refresh token successfully with Basic auth")
  void testRevokeTokenSuccessBasicAuth() {
    // Arrange
    List<String> scopes = Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL, SCOPE_PHONE);
    String refreshToken =
        insertOidcRefreshToken(
            tenant1,
            validClientId,
            TEST_USER_ID,
            REFRESH_TOKEN_EXPIRY_SECONDS,
            scopes,
            true,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS);

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN, refreshToken);

    // Act
    Response response = revokeToken(tenant1, headers, formParams);

    // Assert
    response.then().statusCode(SC_OK);
    boolean isActive = DbUtils.isOidcRefreshTokenActive(tenant1, validClientId, refreshToken);
    assertFalse(isActive, "Refresh token should be inactive after revocation");
  }

  @Test
  @DisplayName("Should return 200 for invalid refresh token")
  void testRevokeTokenInvalidToken() {
    // Arrange
    String invalidToken = "invalid-refresh-token-12345";
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN, invalidToken);

    // Act
    Response response = revokeToken(tenant1, headers, formParams);

    // Assert - Even invalid tokens should return 200 as per OAuth 2.0 spec (RFC 7009)
    response.then().statusCode(SC_OK);
  }

  @Test
  @DisplayName("Should return error for missing authorization header")
  void testRevokeTokenMissingAuth() {
    // Arrange
    List<String> scopes = Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL);
    String refreshToken =
        insertOidcRefreshToken(
            tenant1,
            validClientId,
            TEST_USER_ID,
            REFRESH_TOKEN_EXPIRY_SECONDS,
            scopes,
            true,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS);

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN, refreshToken);

    // Act
    Response response = revokeToken(tenant1, headers, formParams);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR, equalTo(INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo("Authorization header parameter is missing or malformed"));
  }

  @Test
  @DisplayName("Should return error for invalid client credentials")
  void testRevokeTokenInvalidCredentials() {
    // Arrange
    List<String> scopes = Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL);
    String refreshToken =
        insertOidcRefreshToken(
            tenant1,
            validClientId,
            TEST_USER_ID,
            REFRESH_TOKEN_EXPIRY_SECONDS,
            scopes,
            true,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS);

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(INVALID_CLIENT_ID, INVALID_CLIENT_SECRET));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN, refreshToken);

    // Act
    Response response = revokeToken(tenant1, headers, formParams);

    // Assert
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .body(ERROR, equalTo(TOKEN_ERROR_INVALID_CLIENT))
        .body(ERROR_DESCRIPTION, equalTo(TOKEN_ERROR_MSG_CLIENT_AUTH_FAILED));
  }

  @Test
  @DisplayName("Should return error for empty token parameter")
  void testRevokeTokenEmptyToken() {
    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN, ""); // Empty token

    // Act
    Response response = revokeToken(tenant1, headers, formParams);

    // Assert
    response.then().statusCode(SC_BAD_REQUEST).body(ERROR, equalTo(INVALID_REQUEST));
  }

  @Test
  @DisplayName("Should return error for malformed authorization header")
  void testRevokeTokenMalformedAuthHeader() {
    // Arrange
    List<String> scopes = Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL);
    String refreshToken =
        insertOidcRefreshToken(
            tenant1,
            validClientId,
            TEST_USER_ID,
            REFRESH_TOKEN_EXPIRY_SECONDS,
            scopes,
            true,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS);

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, "InvalidAuthHeader"); // Malformed header
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN, refreshToken);

    // Act
    Response response = revokeToken(tenant1, headers, formParams);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .body(ERROR, equalTo(INVALID_REQUEST))
        .body(ERROR_DESCRIPTION, equalTo("Authorization header parameter is missing or malformed"));
  }

  @Test
  @DisplayName(
      "Should not revoke token if token belongs to different client, but should respond successfully")
  void testRevokeTokenDifferentClientToken() {
    // Arrange - Create another client
    Map<String, Object> requestBody = ClientUtils.createValidClientRequest();
    requestBody.put(
        GRANT_TYPES, Arrays.asList(AUTHORIZATION_CODE, CLIENT_CREDENTIALS, REFRESH_TOKEN));
    requestBody.put(CLIENT_NAME, AUTH_TEST_CLIENT_NAME + " 2");
    Response client2Response = createClient(tenant1, requestBody);
    String client2Id = client2Response.jsonPath().getString(CLIENT_ID);

    // Create token for client2 but try to revoke with client1 credentials
    List<String> scopes = Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL);
    String refreshToken =
        insertOidcRefreshToken(
            tenant1,
            client2Id, // Token belongs to client2
            TEST_USER_ID,
            REFRESH_TOKEN_EXPIRY_SECONDS,
            scopes,
            true,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS);

    Map<String, String> headers = new HashMap<>();
    headers.put(
        HEADER_AUTHORIZATION,
        getBasicAuthHeader(validClientId, validClientSecret)); // Using client1 credentials
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN, refreshToken);

    // Act
    Response response = revokeToken(tenant1, headers, formParams);

    // Assert - Should still return 200 as per OAuth 2.0 spec, even if token doesn't belong to the
    // client
    response.then().statusCode(SC_OK);
    // Check in database if the token is revoked
    boolean isRevoked = DbUtils.isRefreshTokenRevoked(refreshToken, tenant1);
    assertFalse(isRevoked, "Refresh token should be not be revoked in redis");
    boolean isActive = DbUtils.isOidcRefreshTokenActive(tenant1, client2Id, refreshToken);
    assertTrue(isActive, "Refresh token should still be active since it belongs to another client");
  }

  @Test
  @DisplayName("Should return 200 for already revoked token")
  void testAlreadyRevokedToken() {
    // Arrange
    List<String> scopes = Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL);
    String refreshToken =
        insertOidcRefreshToken(
            tenant1,
            validClientId,
            TEST_USER_ID,
            REFRESH_TOKEN_EXPIRY_SECONDS,
            scopes,
            false, // Already marked as revoked
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS);

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN, refreshToken);

    // Act
    Response response = revokeToken(tenant1, headers, formParams);

    // Assert - Even invalid tokens should return 200 as per OAuth 2.0 spec (RFC 7009)
    response.then().statusCode(SC_OK);

    // Check in database if the token is remains revoked
    boolean isActive = DbUtils.isOidcRefreshTokenActive(tenant1, validClientId, refreshToken);
    assertFalse(
        isActive, "Refresh token should still be active since it belongs to another client");
    boolean isRevoked = DbUtils.isRefreshTokenRevoked(refreshToken, tenant1);
    assertTrue(isRevoked, "Refresh token should be not be revoked in redis");
  }

  @Test
  @DisplayName("Should return error for null token parameter")
  void testRevokeTokenNullToken() {
    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN, null); // null token

    // Act
    Response response = revokeToken(tenant1, headers, formParams);

    // Assert
    response.then().statusCode(SC_BAD_REQUEST).body(ERROR, equalTo(INVALID_REQUEST));
  }

  @Test
  @DisplayName("Should return error for empty body")
  void testRevokeTokenEmptyBody() {
    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, getBasicAuthHeader(validClientId, validClientSecret));
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>(); // token not supplied
    formParams.put("invalidParam", "invalidValue"); // Just to ensure body is not empty

    // Act
    Response response = revokeToken(tenant1, headers, formParams);

    // Assert
    response.then().statusCode(SC_BAD_REQUEST).body(ERROR, equalTo(INVALID_REQUEST));
  }

  @Test
  @DisplayName("Should return error as unauthorized for malformed Basic auth header")
  void testRevokeTokenMalformedBasicAuthHeader() {
    List<String> scopes = Arrays.asList(SCOPE_OPENID, SCOPE_EMAIL);
    String refreshToken =
        insertOidcRefreshToken(
            tenant1,
            validClientId,
            TEST_USER_ID,
            REFRESH_TOKEN_EXPIRY_SECONDS,
            scopes,
            true,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS);
    // Arrange
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_AUTHORIZATION, AUTH_BASIC_PREFIX + "invalidAuthHeader");
    headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);
    Map<String, String> formParams = new HashMap<>();
    formParams.put(TOKEN, refreshToken);

    // Act
    Response response = revokeToken(tenant1, headers, formParams);

    // Assert
    response.then().statusCode(SC_UNAUTHORIZED).body(ERROR, equalTo(ERROR_INVALID_CLIENT));
  }
}
